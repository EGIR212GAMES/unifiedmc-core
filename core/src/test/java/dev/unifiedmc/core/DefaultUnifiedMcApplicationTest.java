package dev.unifiedmc.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.unifiedmc.compat.CompatibilityEngine;
import dev.unifiedmc.compat.CompatibilityLevel;
import dev.unifiedmc.compat.CompatibilityRequest;
import dev.unifiedmc.compat.CompatibilityResult;
import dev.unifiedmc.config.ConfigurationService;
import dev.unifiedmc.config.CoreConfiguration;
import dev.unifiedmc.dependency.DependencyResolver;
import dev.unifiedmc.dependency.ResolutionResult;
import dev.unifiedmc.mod.ModDescriptor;
import dev.unifiedmc.mod.manager.ModManager;
import dev.unifiedmc.mod.manager.ModScanResult;
import dev.unifiedmc.runtime.BackendDescriptor;
import dev.unifiedmc.runtime.BackendId;
import dev.unifiedmc.runtime.BackendLaunchRequest;
import dev.unifiedmc.runtime.BackendState;
import dev.unifiedmc.runtime.JavaRuntimeDescriptor;
import dev.unifiedmc.runtime.JavaRuntimeManager;
import dev.unifiedmc.runtime.JavaRuntimeValidation;
import dev.unifiedmc.runtime.RuntimeCapabilities;
import dev.unifiedmc.runtime.RuntimeHealth;
import dev.unifiedmc.runtime.RuntimeHealthStatus;
import dev.unifiedmc.runtime.UnifiedBackend;
import dev.unifiedmc.runtime.manager.DefaultRuntimeManager;
import dev.unifiedmc.version.GameVersion;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultUnifiedMcApplicationTest {
    @TempDir Path tempDir;

    @Test
    void invalidConfigFailsAtConfigLoad() throws Exception {
        Path config = tempDir.resolve("invalid.toml");
        Files.writeString(config, "[server\nname = broken\n");
        var app =
                application(
                        new FixedModManager(),
                        new FixedDependencyResolver(),
                        new ReadyCompatibilityEngine(),
                        javaManager(25),
                        backend(false, false));

        StartupFailure failure = assertThrows(StartupFailure.class, () -> app.start(config));

        assertEquals(FailureClass.CONFIGURATION, failure.failureClass());
        assertEquals(LifecyclePhase.CONFIG_LOAD, failure.phase());
    }

    @Test
    void missingRuntimeFailsAtRuntimeDiscovery() throws Exception {
        Path config = config("fixture");
        var app =
                application(
                        new FixedModManager(),
                        new FixedDependencyResolver(),
                        new ReadyCompatibilityEngine(),
                        new EmptyJavaManager(),
                        backend(false, false));

        StartupFailure failure = assertThrows(StartupFailure.class, () -> app.start(config));

        assertEquals(FailureClass.RUNTIME_DISCOVERY, failure.failureClass());
        assertEquals(LifecyclePhase.RUNTIME_DISCOVERY, failure.phase());
    }

    @Test
    void missingDependencyFailsAtDependencyResolution() throws Exception {
        Path config = config("fixture");
        DependencyResolver resolver =
                (mods, game, loader) ->
                        new ResolutionResult(
                                ResolutionResult.Status.MISSING,
                                List.of("Missing dependency: fixture-lib"));
        var app =
                application(
                        new FixedModManager(),
                        resolver,
                        new ReadyCompatibilityEngine(),
                        javaManager(25),
                        backend(false, false));

        StartupFailure failure = assertThrows(StartupFailure.class, () -> app.start(config));

        assertEquals(FailureClass.DEPENDENCY, failure.failureClass());
        assertEquals(LifecyclePhase.DEPENDENCY_RESOLUTION, failure.phase());
        assertTrue(failure.getMessage().contains("fixture-lib"));
    }

    @Test
    void duplicateModFailsAtModDiscovery() throws Exception {
        Path config = config("fixture");
        FixedModManager mods = new FixedModManager();
        mods.scan =
                new ModScanResult(
                        ModScanResult.Status.DUPLICATE,
                        List.of(),
                        List.of("Duplicate mod id: testmod"));
        var app =
                application(
                        mods,
                        new FixedDependencyResolver(),
                        new ReadyCompatibilityEngine(),
                        javaManager(25),
                        backend(false, false));

        StartupFailure failure = assertThrows(StartupFailure.class, () -> app.start(config));

        assertEquals(FailureClass.MOD_DISCOVERY, failure.failureClass());
        assertEquals(LifecyclePhase.MOD_DISCOVERY, failure.phase());
    }

    @Test
    void gracefulShutdownStopsBackend() throws Exception {
        Path config = config("fixture");
        TestBackend backend = backend(false, false);
        var collector = new LifecycleEventCollector();
        var app =
                application(
                        new FixedModManager(),
                        new FixedDependencyResolver(),
                        new ReadyCompatibilityEngine(),
                        javaManager(25),
                        backend,
                        collector);

        app.start(config);
        assertEquals(CoreApplicationState.READY, app.state());
        assertEquals(BackendState.RUNNING, backend.state());
        assertTrue(
                collector.events().stream()
                        .anyMatch(
                                event ->
                                        event.phase() == LifecyclePhase.READY
                                                && event.type() == LifecycleEventType.COMPLETED));

        app.stop();

        assertEquals(CoreApplicationState.STOPPED, app.state());
        assertEquals(BackendState.STOPPED, backend.state());
    }

    @Test
    void failedStartupIsClassifiedAsBackendStartup() throws Exception {
        Path config = config("fixture");
        TestBackend backend = backend(true, false);
        var app =
                application(
                        new FixedModManager(),
                        new FixedDependencyResolver(),
                        new ReadyCompatibilityEngine(),
                        javaManager(25),
                        backend);

        StartupFailure failure = assertThrows(StartupFailure.class, () -> app.start(config));

        assertEquals(FailureClass.BACKEND_STARTUP, failure.failureClass());
        assertEquals(LifecyclePhase.SERVER_START, failure.phase());
        assertEquals(CoreApplicationState.FAILED, app.state());
    }

    @Test
    void failedBackendStateIsClassifiedAsBackendStartup() throws Exception {
        Path config = config("fixture");
        TestBackend backend = backend(false, true);
        var app =
                application(
                        new FixedModManager(),
                        new FixedDependencyResolver(),
                        new ReadyCompatibilityEngine(),
                        javaManager(25),
                        backend);

        StartupFailure failure = assertThrows(StartupFailure.class, () -> app.start(config));

        assertEquals(FailureClass.BACKEND_STARTUP, failure.failureClass());
        assertTrue(failure.getMessage().contains("FAILED"));
    }

    private Path config(String backendId) throws Exception {
        Path config = tempDir.resolve("unifiedmc.toml");
        String backends = tomlPath(tempDir.resolve("backends"));
        String packs = tomlPath(tempDir.resolve("packs"));
        String runtimes = tomlPath(tempDir.resolve("runtimes"));
        String logs = tomlPath(tempDir.resolve("logs").resolve("unifiedmc.log"));
        String fabric = tomlPath(tempDir.resolve("FabricMods"));
        String forge = tomlPath(tempDir.resolve("ForgeMods"));
        String neoforge = tomlPath(tempDir.resolve("NeoForgeMods"));
        String text =
                """
                schema = "unifiedmc-2"

                [server]
                name = "UnifiedMC Test"
                motd = "UnifiedMC Test"
                bind = "127.0.0.1"
                java-port = 25565
                bedrock-port = 19132

                [runtime]
                primary-version = "26.3"
                default-backend = "%s"
                allow-multi-runtime = false

                [mods]
                fabric-directory = "%s"
                forge-directory = "%s"
                neoforge-directory = "%s"
                auto-download-dependencies = false
                verify-signatures = true
                strict-dependencies = true

                [versions]
                allowed-client-versions = ["26.3"]
                backend-versions = ["26.3"]

                [compatibility]
                enabled = false
                mode = "strict"
                fail-on-unsupported = true
                allow-partial-support = false

                [bedrock]
                enabled = false
                geyser = false
                custom-content = false
                resource-packs = false

                [protocol]
                enabled = false
                minimum-client = "26.3"
                maximum-client = "26.3"

                [security]
                allow-unsigned-mods = false
                isolated-runtimes = true
                checksum-policy = "required"

                [logging]
                level = "INFO"
                json = false
                file = "%s"

                [storage]
                backend-directory = "%s"
                pack-directory = "%s"
                runtime-directory = "%s"
                """
                        .formatted(
                                backendId, fabric, forge, neoforge, logs, backends, packs,
                                runtimes);
        return write(config, text);
    }

    private String tomlPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private Path write(Path path, String text) throws Exception {
        Files.writeString(path, text);
        return path;
    }

    private DefaultUnifiedMcApplication application(
            ModManager mods,
            DependencyResolver resolver,
            CompatibilityEngine compatibility,
            JavaRuntimeManager java,
            TestBackend backend) {
        return application(
                mods, resolver, compatibility, java, backend, new LifecycleEventCollector());
    }

    private DefaultUnifiedMcApplication application(
            ModManager mods,
            DependencyResolver resolver,
            CompatibilityEngine compatibility,
            JavaRuntimeManager java,
            TestBackend backend,
            LifecycleEventSink sink) {
        DefaultRuntimeManager runtimeManager = new DefaultRuntimeManager(java);
        runtimeManager.register(backend);
        return new DefaultUnifiedMcApplication(
                new ConfigurationService(),
                runtimeManager,
                mods,
                resolver,
                compatibility,
                new ResourcePreparation() {
                    @Override
                    public ResourcePreparationResult prepare(
                            CoreConfiguration configuration, BackendDescriptor selected) {
                        return new ResourcePreparationResult(
                                ResourcePreparationResult.Status.NOT_REQUIRED, List.of());
                    }
                },
                sink,
                new JdkStructuredLogger(false),
                () -> 25);
    }

    private static JavaRuntimeManager javaManager(int version) {
        Path home = Path.of(System.getProperty("java.home"));
        Path executable = home.resolve("bin").resolve(isWindows() ? "java.exe" : "java");
        JavaRuntimeDescriptor descriptor =
                new JavaRuntimeDescriptor(
                        version,
                        home,
                        executable,
                        "fixture",
                        System.getProperty("os.arch", "unknown"),
                        "fixture",
                        true,
                        Set.of("process-launch"));
        return new FixedJavaManager(descriptor);
    }

    private static TestBackend backend(boolean throwOnStart, boolean failState) {
        return new TestBackend(throwOnStart, failState);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static final class FixedJavaManager implements JavaRuntimeManager {
        private final JavaRuntimeDescriptor runtime;

        private FixedJavaManager(JavaRuntimeDescriptor runtime) {
            this.runtime = runtime;
        }

        @Override
        public List<JavaRuntimeDescriptor> discover() {
            return List.of(runtime);
        }

        @Override
        public Optional<JavaRuntimeDescriptor> select(JavaRuntimeRequirement requirement) {
            return validate(runtime, requirement).accepted()
                    ? Optional.of(runtime)
                    : Optional.empty();
        }

        @Override
        public JavaRuntimeValidation validate(
                JavaRuntimeDescriptor runtime, JavaRuntimeRequirement requirement) {
            return requirement.accepts(runtime.javaVersion())
                    ? JavaRuntimeValidation.accepted("fixture")
                    : JavaRuntimeValidation.rejected("fixture mismatch");
        }
    }

    private static final class EmptyJavaManager implements JavaRuntimeManager {
        @Override
        public List<JavaRuntimeDescriptor> discover() {
            return List.of();
        }

        @Override
        public Optional<JavaRuntimeDescriptor> select(JavaRuntimeRequirement requirement) {
            return Optional.empty();
        }

        @Override
        public JavaRuntimeValidation validate(
                JavaRuntimeDescriptor runtime, JavaRuntimeRequirement requirement) {
            return JavaRuntimeValidation.rejected("empty");
        }
    }

    private static final class FixedDependencyResolver implements DependencyResolver {
        @Override
        public ResolutionResult resolve(
                List<ModDescriptor> mods, String gameVersion, String loader) {
            return new ResolutionResult(ResolutionResult.Status.RESOLVED, List.of());
        }
    }

    private static final class ReadyCompatibilityEngine implements CompatibilityEngine {
        @Override
        public CompatibilityResult evaluate(CompatibilityRequest request) {
            return new CompatibilityResult(CompatibilityLevel.NATIVE, List.of());
        }
    }

    private static final class FixedModManager implements ModManager {
        private ModScanResult scan =
                new ModScanResult(ModScanResult.Status.EMPTY, List.of(), List.of());

        @Override
        public void configure(dev.unifiedmc.config.model.ModConfig configuration) {}

        @Override
        public void refresh() {}

        @Override
        public ModScanResult lastScan() {
            return scan;
        }

        @Override
        public List<ModDescriptor> list() {
            return scan.mods();
        }

        @Override
        public dev.unifiedmc.config.model.ModConfig configuration() {
            return null;
        }
    }

    private static final class TestBackend implements UnifiedBackend {
        private final BackendDescriptor descriptor =
                new BackendDescriptor(
                        new BackendId("fixture"),
                        "26.3",
                        "neoforge",
                        "fixture",
                        new JavaRuntimeRequirement(new GameVersion("26.3"), 25, 25, "fixture"));
        private final boolean throwOnStart;
        private final boolean failState;
        private volatile BackendState state = BackendState.STOPPED;

        private TestBackend(boolean throwOnStart, boolean failState) {
            this.throwOnStart = throwOnStart;
            this.failState = failState;
        }

        @Override
        public BackendDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public RuntimeCapabilities capabilities() {
            return new RuntimeCapabilities(Set.of("fixture"));
        }

        @Override
        public BackendState state() {
            return state;
        }

        @Override
        public RuntimeHealth health() {
            return new RuntimeHealth(
                    state == BackendState.RUNNING
                            ? RuntimeHealthStatus.HEALTHY
                            : RuntimeHealthStatus.STOPPED,
                    java.time.Instant.now(),
                    -1,
                    "Test backend " + state,
                    Optional.empty());
        }

        @Override
        public void start(JavaRuntimeDescriptor javaRuntime, BackendLaunchRequest request) {
            state = BackendState.STARTING;
            if (throwOnStart) {
                state = BackendState.FAILED;
                throw new IllegalStateException("fixture backend failed to start");
            }
            if (failState) {
                state = BackendState.FAILED;
                return;
            }
            state = BackendState.RUNNING;
        }

        @Override
        public void stop() {
            state = BackendState.STOPPING;
            state = BackendState.STOPPED;
        }
    }
}
