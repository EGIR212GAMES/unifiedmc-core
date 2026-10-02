package dev.unifiedmc.cli;

import dev.unifiedmc.compat.engine.ExplicitCompatibilityEngine;
import dev.unifiedmc.config.ConfigException;
import dev.unifiedmc.config.ConfigurationService;
import dev.unifiedmc.config.CoreConfiguration;
import dev.unifiedmc.config.validation.ConfigDiagnostic;
import dev.unifiedmc.core.DefaultResourcePreparation;
import dev.unifiedmc.core.DefaultUnifiedMcApplication;
import dev.unifiedmc.core.JdkStructuredLogger;
import dev.unifiedmc.core.StartupFailure;
import dev.unifiedmc.core.StructuredLogger;
import dev.unifiedmc.core.UnifiedMcApplication;
import dev.unifiedmc.dependency.UnsupportedDependencyResolver;
import dev.unifiedmc.legacy.LegacyPaths;
import dev.unifiedmc.legacy.LegacyRuntimeCatalog;
import dev.unifiedmc.mod.ModLoader;
import dev.unifiedmc.mod.manager.DefaultModManager;
import dev.unifiedmc.mod.manager.ModDiagnosticsJson;
import dev.unifiedmc.runtime.JavaRuntimeDescriptor;
import dev.unifiedmc.runtime.RuntimeBackend;
import dev.unifiedmc.runtime.RuntimeInstallationRequest;
import dev.unifiedmc.runtime.manager.ControlledRuntimeInstaller;
import dev.unifiedmc.runtime.manager.DefaultJavaRuntimeManager;
import dev.unifiedmc.runtime.manager.DefaultRuntimeCatalog;
import dev.unifiedmc.runtime.manager.DefaultRuntimeManager;
import dev.unifiedmc.runtime.manager.DefaultRuntimeRegistry;
import dev.unifiedmc.runtime.manager.RuntimeManager;
import dev.unifiedmc.runtime.manager.RuntimePaths;
import dev.unifiedmc.version.GameVersion;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import dev.unifiedmc.version.RuntimeJavaCompatibility;
import dev.unifiedmc.version.RuntimeVersion;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** UnifiedMC command-line entry point and foreground lifecycle driver. */
public final class Main {
    private static final Path DEFAULT_CONFIG = Path.of("config", "unifiedmc.toml");
    private static final int EXIT_USAGE = 2;
    private static final int EXIT_OPERATIONAL_FAILURE = 1;

    private Main() {}

    public static void main(String[] args) {
        int exit = new Main().run(args);
        if (exit != 0) {
            System.exit(exit);
        }
    }

    int run(String[] args) {
        if (args.length == 0) {
            printUsage();
            return EXIT_USAGE;
        }
        try {
            return dispatch(args);
        } catch (StartupFailure failure) {
            System.err.println("Startup failed");
            System.err.println("  phase: " + failure.phase());
            System.err.println("  class: " + failure.failureClass());
            System.err.println("  message: " + failure.getMessage());
            return EXIT_OPERATIONAL_FAILURE;
        } catch (ConfigException exception) {
            for (ConfigDiagnostic diagnostic : exception.diagnostics()) {
                printDiagnostic(diagnostic);
            }
            return EXIT_OPERATIONAL_FAILURE;
        } catch (Exception exception) {
            System.err.println("Command failed: " + exception.getMessage());
            return EXIT_OPERATIONAL_FAILURE;
        }
    }

    private int dispatch(String[] args) throws Exception {
        return switch (args[0]) {
            case "start" -> start(resolveConfig(args));
            case "stop" -> stop(resolveConfig(args));
            case "restart" -> restart(resolveConfig(args));
            case "status" -> status(resolveConfig(args));
            case "doctor" -> doctor();
            case "config" -> handleConfig(args);
            case "mods" -> handleMods(args, resolveConfig(args));
            case "versions" -> versions();
            case "runtimes" -> handleRuntimes(args);
            case "help", "--help", "-h" -> {
                printUsage();
                yield 0;
            }
            default -> {
                printUsage();
                yield EXIT_USAGE;
            }
        };
    }

    private int start(Path configPath) throws InterruptedException {
        ConfigurationService configurationService = new ConfigurationService();
        CoreConfiguration config = configurationService.load(configPath);
        RuntimeManager runtimeManager = new DefaultRuntimeManager();
        DefaultModManager modManager = new DefaultModManager(config.mods());
        StructuredLogger logger = new JdkStructuredLogger(config.logging().json());
        UnifiedMcApplication application = new DefaultUnifiedMcApplication(
                configurationService,
                runtimeManager,
                modManager,
                new UnsupportedDependencyResolver(),
                new ExplicitCompatibilityEngine(),
                new DefaultResourcePreparation(),
                event -> logger.info("lifecycle.event", Map.of(
                        "phase", event.phase().name(),
                        "type", event.type().name(),
                        "message", event.message(),
                        "durationMs", event.duration().toMillis())),
                logger);
        application.start(configPath);
        System.out.println("UnifiedMC Core READY");
        application.awaitTermination();
        return 0;
    }

    private int stop(Path configPath) throws Exception {
        ConfigurationService service = new ConfigurationService();
        CoreConfiguration config = service.load(configPath);
        var store = new dev.unifiedmc.core.LifecycleStateStore(
                Path.of(config.storage().backendDirectory(), ".unifiedmc-status"));
        Optional<Map<String, String>> state = store.read();
        if (state.isEmpty()) {
            System.out.println("UnifiedMC Core is not running (no state file).");
            return 0;
        }
        String pidValue = state.get().get("pid");
        if (pidValue == null || pidValue.isBlank()) {
            System.out.println("UnifiedMC Core state exists but no PID is recorded; manual recovery is required.");
            return EXIT_OPERATIONAL_FAILURE;
        }
        long pid = Long.parseLong(pidValue);
        Optional<ProcessHandle> process = ProcessHandle.of(pid);
        if (process.isEmpty() || !process.get().isAlive()) {
            System.out.println("UnifiedMC Core is not running; stale state file detected.");
            store.write(dev.unifiedmc.core.CoreApplicationState.STOPPED, state.get().get("backend"), "Stale process state detected");
            return 0;
        }
        String expectedStart = state.get().get("process-start");
        if (expectedStart != null && !expectedStart.isBlank()) {
            var actualStart = process.get().info().startInstant();
            if (actualStart.isEmpty() || !actualStart.get().equals(Instant.parse(expectedStart))) {
                System.err.println("PID does not match the recorded Core process identity; refusing to signal it.");
                return EXIT_OPERATIONAL_FAILURE;
            }
        }
        if (pid == ProcessHandle.current().pid()) {
            return EXIT_OPERATIONAL_FAILURE;
        }
        System.out.println("Requesting graceful shutdown of PID " + pid + "...");
        process.get().destroy();
        boolean exited = process.get().onExit().get(15, TimeUnit.SECONDS).isAlive() == false;
        if (!exited) {
            System.err.println("Graceful shutdown timed out; refusing automatic force-kill.");
            return EXIT_OPERATIONAL_FAILURE;
        }
        System.out.println("UnifiedMC Core stopped.");
        return 0;
    }

    private int restart(Path configPath) throws Exception {
        stop(configPath);
        return start(configPath);
    }

    private int status(Path configPath) throws Exception {
        ConfigurationService service = new ConfigurationService();
        CoreConfiguration config = service.load(configPath);
        var store = new dev.unifiedmc.core.LifecycleStateStore(
                Path.of(config.storage().backendDirectory(), ".unifiedmc-status"));
        Optional<Map<String, String>> state = store.read();
        if (state.isEmpty()) {
            System.out.println("State: STOPPED (no state file)");
            return 0;
        }
        System.out.println("State: " + state.get().getOrDefault("state", "UNKNOWN"));
        System.out.println("PID: " + state.get().getOrDefault("pid", ""));
        System.out.println("Backend: " + state.get().getOrDefault("backend", ""));
        System.out.println("Timestamp: " + state.get().getOrDefault("timestamp", ""));
        System.out.println("Diagnostic: " + state.get().getOrDefault("diagnostic", ""));
        System.out.println("Health: " + state.get().getOrDefault("health", "UNKNOWN"));
        System.out.println("Runtime: " + state.get().getOrDefault("runtime-status", "UNKNOWN"));
        System.out.println("Mods: " + state.get().getOrDefault("mod-status", "UNKNOWN"));
        System.out.println("Compatibility: " + state.get().getOrDefault("compatibility-status", "UNKNOWN"));
        String pidValue = state.get().get("pid");
        if (pidValue != null && !pidValue.isBlank()) {
            Optional<ProcessHandle> process = ProcessHandle.of(Long.parseLong(pidValue));
            System.out.println("Process: " + (process.filter(ProcessHandle::isAlive).isPresent() ? "ALIVE" : "NOT ALIVE"));
        }
        return 0;
    }

    private int handleConfig(String[] args) {
        ConfigurationService service = new ConfigurationService();
        if (args.length < 2) {
            printUsage();
            return EXIT_USAGE;
        }
        return switch (args[1]) {
            case "validate" -> validate(service, resolveConfig(args));
            case "generate" -> generate(service, resolveConfig(args), hasFlag(args, "--force"));
            default -> {
                printUsage();
                yield EXIT_USAGE;
            }
        };
    }

    private int handleMods(String[] args, Path configPath) {
        ConfigurationService service = new ConfigurationService();
        CoreConfiguration config = service.load(configPath);
        DefaultModManager manager = new DefaultModManager(config.mods(), configuredModRuntimes(config));
        if (args.length < 2) {
            printUsage();
            return EXIT_USAGE;
        }
        boolean json = hasFlag(args, "--json");
        manager.refresh();
        var result = manager.lastScan();
        return switch (args[1]) {
            case "scan" -> {
                if (json) {
                    System.out.println(new ModDiagnosticsJson().render(result.diagnosticsReport()));
                }
                else {
                    System.out.println("Mod scan: " + result.status());
                    result.analyses().forEach(a -> System.out.println(formatAnalysis(a)));
                    result.diagnostics().forEach(diagnostic -> System.out.println("diagnostic: " + diagnostic));
                }
                yield modCommandExit(result);
            }
            case "list" -> {
                if (json) {
                    System.out.println(new ModDiagnosticsJson().render(result.diagnosticsReport()));
                }
                else {
                    result.analyses().forEach(a -> System.out.println(formatAnalysis(a)));
                }
                yield modCommandExit(result);
            }
            case "tree" -> {
                if (json) {
                    System.out.println(new ModDiagnosticsJson().render(result.diagnosticsReport()));
                }
                else {
                    printModTree(result);
                }
                yield modCommandExit(result);
            }
            case "doctor" -> {
                if (json) {
                    System.out.println(new ModDiagnosticsJson().render(result.diagnosticsReport()));
                }
                else {
                    printModDoctor(result);
                }
                yield modCommandExit(result);
            }
            default -> {
                printUsage();
                yield EXIT_USAGE;
            }
        };
    }


    private List<RuntimeVersion> configuredModRuntimes(CoreConfiguration config) {
        String version = config.runtime().primaryVersion();
        String backend = config.runtime().defaultBackend().toLowerCase(java.util.Locale.ROOT);
        ModLoader loader = backend.contains("neoforge") ? ModLoader.NEOFORGE
                : backend.contains("fabric") ? ModLoader.FABRIC
                : backend.contains("forge") ? ModLoader.FORGE : ModLoader.UNKNOWN;
        JavaRuntimeRequirement requirement = RuntimeJavaCompatibility.officialBaseline().requirementFor(version)
                .orElse(new JavaRuntimeRequirement(new GameVersion(version), 8, 8, "not-catalogued"));
        if (loader == ModLoader.UNKNOWN) {
            return List.of();
        }
        return List.of(new RuntimeVersion(new GameVersion(version), loader.name().toLowerCase(java.util.Locale.ROOT), backend, requirement));
    }

    private static String formatAnalysis(dev.unifiedmc.mod.ModAnalysis analysis) {
        var metadata = analysis.metadata();
        return metadata.id().value() + " " + metadata.version() + " loader=" + metadata.loader()
                + " minecraft=" + metadata.minecraftVersion().map(v -> v.value()).orElse(metadata.minecraftVersionConstraint().isBlank() ? "unknown" : metadata.minecraftVersionConstraint())
                + " result=" + analysis.compatibility().status()
                + analysis.compatibility().assignedRuntime().map(value -> " runtime=" + value).orElse("");
    }

    private static int modCommandExit(dev.unifiedmc.mod.manager.ModScanResult result) {
        return switch (result.status()) {
            case READY, EMPTY -> 0;
            default -> EXIT_OPERATIONAL_FAILURE;
        };
    }

    private static void printModTree(dev.unifiedmc.mod.manager.ModScanResult result) {
        try {
            System.out.println("Topological order: " + result.graph().topologicalOrder().stream().map(dev.unifiedmc.mod.ModId::value).toList());
        } catch (IllegalStateException exception) {
            System.out.println("Topological order: FAILED (dependency cycle)");
        }
        for (var analysis : result.analyses()) {
            var metadata = analysis.metadata();
            System.out.println(metadata.id().value() + "@" + metadata.version() + " -> " + analysis.compatibility().status());
            metadata.dependencies().forEach(dep -> System.out.println("  └─ " + dep.modId() + " " + dep.versionConstraint() + (dep.required() ? " [required]" : " [optional]")));
        }
    }

    private static void printModDoctor(dev.unifiedmc.mod.manager.ModScanResult result) {
        System.out.println("Mod Manager status: " + result.status());
        result.analyses().forEach(analysis -> {
            System.out.println(formatAnalysis(analysis));
            System.out.println("  stages=" + analysis.stages());
        });
        if (result.diagnosticsReport().diagnostics().isEmpty()) {
            System.out.println("Diagnostics: none");
        }
        else {
            result.diagnosticsReport().diagnostics().forEach(d -> System.out.println("[" + d.severity() + "] " + d.code() + " path=" + d.path() + " property=" + d.property() + " | " + d.message() + " | fix=" + d.fix()));
        }
    }

    private int versions() {
        for (JavaRuntimeRequirement requirement : RuntimeJavaCompatibility.officialBaseline().all()) {
            System.out.println(requirement.minecraftVersion().value()
                    + " -> Java " + requirement.minimumJava()
                    + " (source: " + requirement.sourceReference() + ")");
        }
        return 0;
    }

    private int handleRuntimes(String[] args) {
        if (args.length < 2 || args[1].equals("list")) {
            return runtimesList();
        }
        return switch (args[1]) {
            case "doctor" -> runtimesDoctor();
            case "install" -> runtimesInstall(args);
            default -> {
                System.out.println("Usage: unifiedmc runtimes list|doctor|install <version> <backend>");
                yield EXIT_USAGE;
            }
        };
    }

    private int runtimesList() {
        DefaultRuntimeRegistry registry = new DefaultRuntimeRegistry(RuntimePaths.defaultRoot());
        registry.discover();
        System.out.println("Runtime registry: " + registry.runtimeRoot());
        for (var catalog : new DefaultRuntimeCatalog().entries()) {
            String key = catalog.gameVersion() + "/" + catalog.backend();
            var installed = registry.list().stream()
                    .filter(runtime -> runtime.metadata().gameVersion().equals(catalog.gameVersion()))
                    .filter(runtime -> runtime.metadata().backend() == catalog.backend())
                    .findFirst();
            System.out.println(key + " " + (installed.isPresent() ? "INSTALLED" : "NOT_INSTALLED")
                    + " java=" + catalog.javaRequirement().map(r -> Integer.toString(r.minimumJava())).orElse("unknown"));
        }
        if (!registry.diagnostics().isEmpty()) {
            registry.diagnostics().forEach(message -> System.out.println("[WARN] " + message));
        }
        System.out.println("Legacy experimental registry: " + LegacyPaths.defaultRoot());
        for (var legacy : new LegacyRuntimeCatalog().entries()) {
            Path root = LegacyPaths.runtime(legacy.game().value());
            boolean manifest = java.nio.file.Files.isRegularFile(root.resolve("installation.json"));
            System.out.println("legacy/" + legacy.game().value() + "/forge " + (manifest ? "MANIFEST_FOUND" : "NOT_INSTALLED")
                    + " java=" + legacy.javaRuntime().minimumJava());
        }
        return 0;
    }

    private int runtimesDoctor() {
        DefaultJavaRuntimeManager javaManager = new DefaultJavaRuntimeManager();
        for (JavaRuntimeDescriptor runtime : javaManager.discover()) {
            System.out.println("Java " + runtime.javaVersion() + " vendor=" + runtime.vendor()
                    + " source=" + runtime.source() + " verified=" + runtime.verified());
        }
        DefaultRuntimeRegistry registry = new DefaultRuntimeRegistry(RuntimePaths.defaultRoot());
        var report = registry.discover();
        report.runtimes().forEach(runtime -> {
            var requirement = runtime.metadata().javaRequirement();
            var selected = javaManager.select(requirement);
            System.out.println(runtime.metadata().runtimeId() + " -> "
                    + (selected.map(java -> "Java " + java.javaVersion() + " OK").orElse("MISSING_JDK")));
        });
        report.diagnostics().forEach(message -> System.out.println("[ERROR] " + message));
        System.out.println("Legacy directories:");
        for (var legacy : new LegacyRuntimeCatalog().entries()) {
            Path root = LegacyPaths.runtime(legacy.game().value());
            System.out.println("  legacy/" + legacy.game().value() + " -> "
                    + (java.nio.file.Files.exists(root) ? "PRESENT" : "MISSING")
                    + ", Java " + legacy.javaRuntime().minimumJava());
        }
        return report.diagnostics().isEmpty() ? 0 : EXIT_OPERATIONAL_FAILURE;
    }

    private int runtimesInstall(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: unifiedmc runtimes install <version> <backend>");
            return EXIT_USAGE;
        }
        RuntimeBackend backend;
        try {
            backend = parseRuntimeBackend(args[3]);
        } catch (IllegalArgumentException exception) {
            System.err.println("Unknown runtime backend: " + args[3]);
            return EXIT_USAGE;
        }
        var result = new ControlledRuntimeInstaller().install(
                new RuntimeInstallationRequest(args[2], backend, RuntimePaths.defaultRoot()));
        System.out.println(result.status() + ": " + result.message());
        result.diagnostics().forEach(diagnostic -> System.out.println("  - " + diagnostic));
        return result.status() == dev.unifiedmc.runtime.RuntimeInstallationStatus.INSTALLED
                || result.status() == dev.unifiedmc.runtime.RuntimeInstallationStatus.ALREADY_INSTALLED ? 0 : EXIT_OPERATIONAL_FAILURE;
    }


    private static RuntimeBackend parseRuntimeBackend(String value) {
        return switch (value.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "neoforge", "modern-neoforge", "modern_neoforge" -> RuntimeBackend.MODERN_NEOFORGE;
            case "fabric", "fabric-via-connector", "fabric_via_connector" -> RuntimeBackend.FABRIC_VIA_CONNECTOR;
            case "forge", "legacy-forge", "legacy_forge" -> RuntimeBackend.LEGACY_FORGE;
            case "custom", "future-custom", "future_custom" -> RuntimeBackend.FUTURE_CUSTOM;
            default -> throw new IllegalArgumentException("Unknown backend");
        };
    }

    private int validate(ConfigurationService service, Path path) {
        ConfigurationService.ConfigLoadResult result = service.inspect(path);
        for (ConfigDiagnostic diagnostic : result.diagnostics()) {
            printDiagnostic(diagnostic);
        }
        if (result.valid()) {
            System.out.println("Configuration valid: " + path);
            if (result.migrated()) {
                System.out.println("Note: older schema migrated in memory; file not rewritten.");
            }
            return 0;
        }
        return EXIT_OPERATIONAL_FAILURE;
    }

    private int generate(ConfigurationService service, Path path, boolean overwrite) {
        try {
            service.generate(path, overwrite);
            System.out.println("Generated default configuration: " + path);
            return 0;
        } catch (ConfigException exception) {
            for (ConfigDiagnostic diagnostic : exception.diagnostics()) {
                printDiagnostic(diagnostic);
            }
            return EXIT_OPERATIONAL_FAILURE;
        } catch (IOException exception) {
            System.err.println("Failed to generate configuration: " + exception.getMessage());
            return EXIT_OPERATIONAL_FAILURE;
        }
    }

    private int doctor() {
        DefaultJavaRuntimeManager manager = new DefaultJavaRuntimeManager();
        List<JavaRuntimeDescriptor> runtimes = manager.discover();
        int coreMajor = Runtime.version().feature();
        System.out.println("## Java Environment");
        System.out.println("Core JDK: " + coreMajor);
        System.out.println("Core JDK Status: " + (coreMajor == 25 ? "OK" : "REJECTED (requires 25)"));
        System.out.println("Discovered JDKs: " + runtimes.stream().map(runtime -> Integer.toString(runtime.javaVersion())).distinct().sorted().toList());
        System.out.println();
        versions();
        return 0;
    }

    private static Path resolveConfig(String[] args) {
        for (int i = 1; i < args.length; i++) {
            if (!args[i].startsWith("--")) {
                if (args[0].equals("config") && i == 1) {
                    continue;
                }
                if ((args[0].equals("mods") || args[0].equals("versions") || args[0].equals("runtimes"))
                        && i == 1 && (args.length > 1)) {
                    continue;
                }
                return Path.of(args[i]);
            }
        }
        return DEFAULT_CONFIG;
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String arg : args) {
            if (arg.equals(flag)) {
                return true;
            }
        }
        return false;
    }

    private static void printDiagnostic(ConfigDiagnostic diagnostic) {
        System.err.println("[" + diagnostic.severity() + "] path=" + diagnostic.path());
        System.err.println("  property: " + diagnostic.property());
        System.err.println("  expected: " + diagnostic.expected());
        System.err.println("  actual:   " + diagnostic.actual());
        System.err.println("  fix:      " + diagnostic.possibleFix());
    }

    private static void printUsage() {
        System.out.println("UnifiedMC Core");
        System.out.println("Usage:");
        System.out.println("  unifiedmc start [config]");
        System.out.println("  unifiedmc stop [config]");
        System.out.println("  unifiedmc restart [config]");
        System.out.println("  unifiedmc status [config]");
        System.out.println("  unifiedmc doctor");
        System.out.println("  unifiedmc config validate [config]");
        System.out.println("  unifiedmc config generate [config] [--force]");
        System.out.println("  unifiedmc mods list [config] [--json]");
        System.out.println("  unifiedmc mods scan [config] [--json]");
        System.out.println("  unifiedmc mods tree [config] [--json]");
        System.out.println("  unifiedmc mods doctor [config] [--json]");
        System.out.println("  unifiedmc versions list");
        System.out.println("  unifiedmc runtimes list");
        System.out.println("  unifiedmc runtimes doctor");
        System.out.println("  unifiedmc runtimes install <version> <backend>");
    }
}
