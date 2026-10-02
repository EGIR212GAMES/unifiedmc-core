package dev.unifiedmc.core;

import dev.unifiedmc.api.health.CapabilityStatus;
import dev.unifiedmc.api.health.CompatibilityStatus;
import dev.unifiedmc.api.health.HealthSnapshot;
import dev.unifiedmc.api.health.HealthStatus;
import dev.unifiedmc.api.health.ModStatus;
import dev.unifiedmc.api.health.RuntimeStatus;
import dev.unifiedmc.compat.CompatibilityEngine;
import dev.unifiedmc.compat.CompatibilityLevel;
import dev.unifiedmc.compat.CompatibilityRequest;
import dev.unifiedmc.config.ConfigException;
import dev.unifiedmc.config.ConfigurationService;
import dev.unifiedmc.config.CoreConfiguration;
import dev.unifiedmc.dependency.DependencyResolver;
import dev.unifiedmc.dependency.ResolutionResult;
import dev.unifiedmc.mod.manager.ModManager;
import dev.unifiedmc.mod.manager.ModScanResult;
import dev.unifiedmc.runtime.BackendDescriptor;
import dev.unifiedmc.runtime.BackendId;
import dev.unifiedmc.runtime.BackendLaunchRequest;
import dev.unifiedmc.runtime.BackendState;
import dev.unifiedmc.runtime.JavaRuntimeManager;
import dev.unifiedmc.runtime.UnifiedBackend;
import dev.unifiedmc.runtime.manager.RuntimeManager;
import dev.unifiedmc.version.GameVersion;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import dev.unifiedmc.version.RuntimeJavaCompatibility;
import dev.unifiedmc.version.RuntimeVersion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.IntSupplier;

/**
 * Deterministic control-plane lifecycle orchestration without concrete Minecraft runtime support.
 */
public final class DefaultUnifiedMcApplication implements UnifiedMcApplication {
    private final ConfigurationService configurationService;
    private final RuntimeManager runtimeManager;
    private final ModManager modManagerFactory;
    private final DependencyResolver dependencyResolver;
    private final CompatibilityEngine compatibilityEngine;
    private final ResourcePreparation resourcePreparation;
    private final LifecycleEventSink eventSink;
    private final StructuredLogger logger;
    private volatile CoreApplicationState state = CoreApplicationState.STOPPED;
    private volatile HealthSnapshot health = stoppedHealth("Not started");
    private volatile StartupDiagnostics diagnostics = new StartupDiagnostics(List.of(), null);
    private volatile CoreConfiguration configuration;
    private volatile BackendId selectedBackend;
    private volatile ShutdownCoordinator shutdownCoordinator;
    private volatile LifecycleStateStore stateStore;
    private final LifecycleEventCollector collector = new LifecycleEventCollector();
    private final IntSupplier coreJavaVersionSupplier;
    private volatile CountDownLatch termination = new CountDownLatch(1);

    public DefaultUnifiedMcApplication(
            ConfigurationService configurationService,
            RuntimeManager runtimeManager,
            ModManager modManager,
            DependencyResolver dependencyResolver,
            CompatibilityEngine compatibilityEngine,
            ResourcePreparation resourcePreparation,
            LifecycleEventSink eventSink,
            StructuredLogger logger) {
        this(
                configurationService,
                runtimeManager,
                modManager,
                dependencyResolver,
                compatibilityEngine,
                resourcePreparation,
                eventSink,
                logger,
                () -> Runtime.version().feature());
    }

    public DefaultUnifiedMcApplication(
            ConfigurationService configurationService,
            RuntimeManager runtimeManager,
            ModManager modManager,
            DependencyResolver dependencyResolver,
            CompatibilityEngine compatibilityEngine,
            ResourcePreparation resourcePreparation,
            LifecycleEventSink eventSink,
            StructuredLogger logger,
            IntSupplier coreJavaVersionSupplier) {
        this.configurationService =
                Objects.requireNonNull(configurationService, "configurationService");
        this.runtimeManager = Objects.requireNonNull(runtimeManager, "runtimeManager");
        this.modManagerFactory = Objects.requireNonNull(modManager, "modManager");
        this.dependencyResolver = Objects.requireNonNull(dependencyResolver, "dependencyResolver");
        this.compatibilityEngine =
                Objects.requireNonNull(compatibilityEngine, "compatibilityEngine");
        this.resourcePreparation =
                Objects.requireNonNull(resourcePreparation, "resourcePreparation");
        this.eventSink = Objects.requireNonNull(eventSink, "eventSink");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.coreJavaVersionSupplier =
                Objects.requireNonNull(coreJavaVersionSupplier, "coreJavaVersionSupplier");
    }

    public DefaultUnifiedMcApplication(
            ConfigurationService configurationService,
            RuntimeManager runtimeManager,
            ModManager modManager,
            DependencyResolver dependencyResolver,
            CompatibilityEngine compatibilityEngine) {
        this(
                configurationService,
                runtimeManager,
                modManager,
                dependencyResolver,
                compatibilityEngine,
                new DefaultResourcePreparation(),
                new LifecycleEventCollector(),
                new JdkStructuredLogger(false));
    }

    @Override
    public synchronized void start(Path configurationPath) {
        if (state == CoreApplicationState.READY || state == CoreApplicationState.STARTING) {
            throw new IllegalStateException("UnifiedMC Core is already running");
        }
        termination = new CountDownLatch(1);
        state = CoreApplicationState.STARTING;
        health = startingHealth();
        lifecycle(
                LifecyclePhase.BOOTSTRAP,
                () -> {
                    collector.events();
                    return null;
                });
        try {
            CoreConfiguration loaded =
                    phase(
                            LifecyclePhase.CONFIG_LOAD,
                            () -> configurationService.load(configurationPath));
            configuration = loaded;
            modManagerFactory.configure(loaded.mods());
            stateStore =
                    new LifecycleStateStore(
                            Path.of(loaded.storage().backendDirectory(), ".unifiedmc-status"));
            safeStateWrite(CoreApplicationState.STARTING, null, "Startup in progress");

            phase(
                    LifecyclePhase.ENVIRONMENT_CHECK,
                    () -> {
                        var validation =
                                CoreJavaPolicy.validate(coreJavaVersionSupplier.getAsInt());
                        if (!validation.accepted()) {
                            throw new IllegalStateException(validation.diagnostic());
                        }
                        return null;
                    });

            phase(
                    LifecyclePhase.RUNTIME_DISCOVERY,
                    () -> {
                        JavaRuntimeManager javaRuntimes = runtimeManager.javaRuntimes();
                        List<?> discovered = javaRuntimes.discover();
                        if (discovered.isEmpty()) {
                            throw new IllegalStateException(
                                    "No verified Java runtimes were discovered");
                        }
                        JavaRuntimeRequirement requirement =
                                RuntimeJavaCompatibility.officialBaseline()
                                        .requirementFor(loaded.runtime().primaryVersion())
                                        .orElseThrow(
                                                () ->
                                                        new IllegalStateException(
                                                                "No official Java runtime requirement is registered for Minecraft "
                                                                        + loaded.runtime()
                                                                                .primaryVersion()));
                        if (javaRuntimes.select(requirement).isEmpty()) {
                            throw new IllegalStateException(
                                    "No verified Java runtime satisfies Minecraft "
                                            + requirement.minecraftVersion().value()
                                            + " (Java "
                                            + requirement.minimumJava()
                                            + ")");
                        }
                        return null;
                    });

            phase(
                    LifecyclePhase.MOD_DISCOVERY,
                    () -> {
                        modManagerFactory.refresh();
                        ModScanResult scan = modManagerFactory.lastScan();
                        if (scan.status() == ModScanResult.Status.DUPLICATE) {
                            throw new IllegalStateException(String.join("; ", scan.diagnostics()));
                        }
                        if (scan.status() == ModScanResult.Status.FAILED) {
                            throw new IllegalStateException(String.join("; ", scan.diagnostics()));
                        }
                        return null;
                    });

            phase(
                    LifecyclePhase.DEPENDENCY_RESOLUTION,
                    () -> {
                        ResolutionResult resolution =
                                dependencyResolver.resolve(
                                        modManagerFactory.list(),
                                        loaded.runtime().primaryVersion(),
                                        loaderFromBackend(loaded.runtime().defaultBackend()));
                        if (!resolution.successful()) {
                            throw new IllegalStateException(
                                    String.join("; ", resolution.diagnostics()));
                        }
                        return null;
                    });

            phase(
                    LifecyclePhase.COMPATIBILITY_ANALYSIS,
                    () -> {
                        if (!loaded.compatibility().enabled()) {
                            health =
                                    withCompatibility(
                                            health,
                                            CompatibilityStatus.DISABLED,
                                            "Compatibility analysis disabled");
                            return null;
                        }
                        health =
                                withCompatibility(
                                        health,
                                        CompatibilityStatus.ANALYZING,
                                        "Compatibility analysis in progress");
                        BackendDescriptor target = backendDescriptorHint(loaded);
                        JavaRuntimeRequirement requirement =
                                target != null
                                        ? target.javaRuntime()
                                        : RuntimeJavaCompatibility.officialBaseline()
                                                .requirementFor(loaded.runtime().primaryVersion())
                                                .orElseThrow(
                                                        () ->
                                                                new IllegalStateException(
                                                                        "Unknown primary runtime version"));
                        RuntimeVersion runtimeVersion =
                                new RuntimeVersion(
                                        new GameVersion(loaded.runtime().primaryVersion()),
                                        loaderFromBackend(loaded.runtime().defaultBackend()),
                                        target == null ? "unknown" : target.loaderVersion(),
                                        requirement);
                        List<String> unsupported = new ArrayList<>();
                        boolean partial = false;
                        for (var mod : modManagerFactory.list()) {
                            var result =
                                    compatibilityEngine.evaluate(
                                            new CompatibilityRequest(mod, runtimeVersion));
                            if (result.level() == CompatibilityLevel.UNSUPPORTED) {
                                unsupported.addAll(result.diagnostics());
                            }
                            if (result.level() == CompatibilityLevel.PARTIAL) {
                                partial = true;
                            }
                        }
                        if (!unsupported.isEmpty() && loaded.compatibility().failOnUnsupported()) {
                            throw new IllegalStateException(String.join("; ", unsupported));
                        }
                        if (partial && !loaded.compatibility().allowPartialSupport()) {
                            throw new IllegalStateException(
                                    "Partial compatibility was detected and allow-partial-support=false");
                        }
                        health =
                                withCompatibility(
                                        health,
                                        partial
                                                ? CompatibilityStatus.PARTIAL
                                                : (unsupported.isEmpty()
                                                        ? CompatibilityStatus.READY
                                                        : CompatibilityStatus.UNSUPPORTED),
                                        unsupported.isEmpty()
                                                ? "Compatibility analysis complete"
                                                : String.join("; ", unsupported));
                        return null;
                    });

            BackendDescriptor backend =
                    phase(
                            LifecyclePhase.BACKEND_SELECTION,
                            () ->
                                    runtimeManager
                                            .find(new BackendId(loaded.runtime().defaultBackend()))
                                            .map(UnifiedBackend::descriptor)
                                            .orElseThrow(
                                                    () ->
                                                            new IllegalStateException(
                                                                    "Default backend is not registered: "
                                                                            + loaded.runtime()
                                                                                    .defaultBackend())));
            selectedBackend = backend.id();

            phase(
                    LifecyclePhase.RESOURCE_PREPARATION,
                    () -> {
                        ResourcePreparationResult result =
                                resourcePreparation.prepare(loaded, backend);
                        if (result.status() == ResourcePreparationResult.Status.UNSUPPORTED
                                || result.status() == ResourcePreparationResult.Status.FAILED) {
                            throw new IllegalStateException(
                                    String.join("; ", result.diagnostics()));
                        }
                        return null;
                    });

            phase(
                    LifecyclePhase.SERVER_START,
                    () -> {
                        Path workingDirectory =
                                Path.of(loaded.storage().backendDirectory(), backend.id().value());
                        Files.createDirectories(workingDirectory);
                        runtimeManager.start(
                                backend.id(),
                                new BackendLaunchRequest(
                                        workingDirectory, List.of(), List.of(), Map.of()));
                        if (runtimeManager.state(backend.id()) == BackendState.FAILED) {
                            throw new IllegalStateException(
                                    "Backend reported FAILED immediately after start: "
                                            + backend.id().value());
                        }
                        return null;
                    });

            phase(LifecyclePhase.READY, () -> null);
            state = CoreApplicationState.READY;
            health =
                    new HealthSnapshot(
                            HealthStatus.HEALTHY,
                            RuntimeStatus.RUNNING,
                            modManagerFactory.list().isEmpty() ? ModStatus.EMPTY : ModStatus.READY,
                            compatibilityStatus(health),
                            capabilities(),
                            Instant.now(),
                            "UnifiedMC Core is ready");
            safeStateWrite(CoreApplicationState.READY, backend.id().value(), "Ready");
            shutdownCoordinator = new ShutdownCoordinator(this::stop);
            shutdownCoordinator.install();
        } catch (StartupFailure failure) {
            state = CoreApplicationState.FAILED;
            health = failedHealth(failure);
            diagnostics = new StartupDiagnostics(collector.events(), failure);
            safeStateWrite(
                    CoreApplicationState.FAILED,
                    selectedBackend == null ? null : selectedBackend.value(),
                    failure.getMessage());
            throw failure;
        } catch (Exception exception) {
            StartupFailure failure =
                    new StartupFailure(
                            LifecyclePhase.SERVER_START,
                            FailureClass.INTERNAL,
                            exception.getMessage() == null
                                    ? exception.getClass().getSimpleName()
                                    : exception.getMessage(),
                            exception);
            state = CoreApplicationState.FAILED;
            health = failedHealth(failure);
            diagnostics = new StartupDiagnostics(collector.events(), failure);
            safeStateWrite(
                    CoreApplicationState.FAILED,
                    selectedBackend == null ? null : selectedBackend.value(),
                    failure.getMessage());
            throw failure;
        }
    }

    @Override
    public synchronized void stop() {
        if (state == CoreApplicationState.STOPPED) {
            return;
        }
        state = CoreApplicationState.STOPPING;
        health =
                new HealthSnapshot(
                        HealthStatus.STARTING,
                        RuntimeStatus.STOPPING,
                        health.mods(),
                        health.compatibility(),
                        health.capabilities(),
                        Instant.now(),
                        "Shutdown in progress");
        try {
            if (selectedBackend != null && runtimeManager.find(selectedBackend).isPresent()) {
                runtimeManager.stop(selectedBackend);
            }
            state = CoreApplicationState.STOPPED;
            health = stoppedHealth("UnifiedMC Core stopped");
            safeStateWrite(
                    CoreApplicationState.STOPPED,
                    selectedBackend == null ? null : selectedBackend.value(),
                    "Stopped");
        } catch (Exception exception) {
            FailureClass failureClass = FailureClass.SHUTDOWN;
            StartupFailure failure =
                    new StartupFailure(
                            LifecyclePhase.SERVER_START,
                            failureClass,
                            exception.getMessage() == null
                                    ? "Shutdown failed"
                                    : exception.getMessage(),
                            exception);
            state = CoreApplicationState.FAILED;
            health = failedHealth(failure);
            diagnostics = new StartupDiagnostics(collector.events(), failure);
        } finally {
            termination.countDown();
            if (shutdownCoordinator != null) {
                shutdownCoordinator.close();
                shutdownCoordinator = null;
            }
        }
    }

    @Override
    public CoreApplicationState state() {
        return state;
    }

    @Override
    public HealthSnapshot health() {
        return health;
    }

    @Override
    public StartupDiagnostics diagnostics() {
        return diagnostics;
    }

    @Override
    public void awaitTermination() throws InterruptedException {
        termination.await();
    }

    public LifecycleStateStore stateStore() {
        return stateStore;
    }

    private <T> T phase(LifecyclePhase phase, PhaseAction<T> action) {
        Instant started = Instant.now();
        eventSink.publish(
                new LifecycleEvent(
                        phase,
                        LifecycleEventType.STARTED,
                        started,
                        Duration.ZERO,
                        phase.name() + " started"));
        collector.publish(
                new LifecycleEvent(
                        phase,
                        LifecycleEventType.STARTED,
                        started,
                        Duration.ZERO,
                        phase.name() + " started"));
        logger.info("lifecycle.phase.started", Map.of("phase", phase.name()));
        try {
            T value = action.run();
            Duration duration = Duration.between(started, Instant.now());
            LifecycleEvent event =
                    new LifecycleEvent(
                            phase,
                            LifecycleEventType.COMPLETED,
                            Instant.now(),
                            duration,
                            phase.name() + " completed");
            eventSink.publish(event);
            collector.publish(event);
            logger.info(
                    "lifecycle.phase.completed",
                    Map.of("phase", phase.name(), "durationMs", duration.toMillis()));
            return value;
        } catch (ConfigException exception) {
            return failPhase(phase, FailureClass.CONFIGURATION, exception, started);
        } catch (Exception exception) {
            return failPhase(phase, classify(phase), exception, started);
        }
    }

    private <T> T lifecycle(LifecyclePhase phase, PhaseAction<T> action) {
        return phase(phase, action);
    }

    private <T> T failPhase(
            LifecyclePhase phase, FailureClass failureClass, Exception exception, Instant started) {
        Duration duration = Duration.between(started, Instant.now());
        LifecycleEvent event =
                new LifecycleEvent(
                        phase,
                        LifecycleEventType.FAILED,
                        Instant.now(),
                        duration,
                        exception.getMessage() == null
                                ? exception.getClass().getSimpleName()
                                : exception.getMessage());
        eventSink.publish(event);
        collector.publish(event);
        logger.error(
                "lifecycle.phase.failed",
                Map.of(
                        "phase",
                        phase.name(),
                        "failureClass",
                        failureClass.name(),
                        "message",
                        exception.getMessage() == null ? "" : exception.getMessage()));
        throw new StartupFailure(phase, failureClass, event.message(), exception);
    }

    private static FailureClass classify(LifecyclePhase phase) {
        return switch (phase) {
            case CONFIG_LOAD -> FailureClass.CONFIGURATION;
            case ENVIRONMENT_CHECK -> FailureClass.ENVIRONMENT;
            case RUNTIME_DISCOVERY -> FailureClass.RUNTIME_DISCOVERY;
            case MOD_DISCOVERY -> FailureClass.MOD_DISCOVERY;
            case DEPENDENCY_RESOLUTION -> FailureClass.DEPENDENCY;
            case COMPATIBILITY_ANALYSIS -> FailureClass.COMPATIBILITY;
            case BACKEND_SELECTION -> FailureClass.BACKEND_SELECTION;
            case RESOURCE_PREPARATION -> FailureClass.RESOURCE_PREPARATION;
            case SERVER_START -> FailureClass.BACKEND_STARTUP;
            case BOOTSTRAP, READY -> FailureClass.INTERNAL;
        };
    }

    private BackendDescriptor backendDescriptorHint(CoreConfiguration config) {
        return runtimeManager
                .find(new BackendId(config.runtime().defaultBackend()))
                .map(UnifiedBackend::descriptor)
                .orElse(null);
    }

    private static String loaderFromBackend(String backend) {
        String normalized = backend.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("fabric")) {
            return "fabric";
        }
        if (normalized.contains("forge") && !normalized.contains("neoforge")) {
            return "forge";
        }
        if (normalized.contains("neo")) {
            return "neoforge";
        }
        return "unknown";
    }

    private Map<String, CapabilityStatus> capabilities() {
        return Map.of(
                "core", CapabilityStatus.AVAILABLE,
                "runtime-orchestration", CapabilityStatus.AVAILABLE,
                "mod-discovery", CapabilityStatus.AVAILABLE,
                "dependency-resolution", CapabilityStatus.DEGRADED,
                "compatibility", CapabilityStatus.DEGRADED,
                "resource-preparation", CapabilityStatus.DEGRADED);
    }

    private HealthSnapshot withCompatibility(
            HealthSnapshot current, CompatibilityStatus status, String diagnostic) {
        return new HealthSnapshot(
                current.overall(),
                current.runtime(),
                current.mods(),
                status,
                current.capabilities(),
                Instant.now(),
                diagnostic);
    }

    private CompatibilityStatus compatibilityStatus(HealthSnapshot snapshot) {
        return snapshot.compatibility();
    }

    private HealthSnapshot startingHealth() {
        return new HealthSnapshot(
                HealthStatus.STARTING,
                RuntimeStatus.DISCOVERING,
                ModStatus.DISCOVERING,
                CompatibilityStatus.ANALYZING,
                capabilities(),
                Instant.now(),
                "Startup in progress");
    }

    private HealthSnapshot failedHealth(StartupFailure failure) {
        return new HealthSnapshot(
                HealthStatus.UNHEALTHY,
                RuntimeStatus.FAILED,
                ModStatus.FAILED,
                CompatibilityStatus.FAILED,
                capabilities(),
                Instant.now(),
                failure.failureClass() + ": " + failure.getMessage());
    }

    private static HealthSnapshot stoppedHealth(String diagnostic) {
        return new HealthSnapshot(
                HealthStatus.STOPPED,
                RuntimeStatus.STOPPED,
                ModStatus.EMPTY,
                CompatibilityStatus.DISABLED,
                Map.of(),
                Instant.now(),
                diagnostic);
    }

    private void safeStateWrite(CoreApplicationState newState, String backend, String diagnostic) {
        if (stateStore == null) {
            return;
        }
        try {
            stateStore.write(newState, backend, diagnostic, health);
        } catch (IOException exception) {
            logger.warn(
                    "lifecycle.state.persist.failed",
                    Map.of(
                            "message",
                            exception.getMessage() == null ? "" : exception.getMessage()));
        }
    }

    private interface PhaseAction<T> {
        T run() throws Exception;
    }
}
