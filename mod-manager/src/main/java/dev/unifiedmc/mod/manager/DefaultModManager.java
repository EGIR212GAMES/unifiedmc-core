package dev.unifiedmc.mod.manager;

import dev.unifiedmc.config.model.ModConfig;
import dev.unifiedmc.mod.AnalysisStage;
import dev.unifiedmc.mod.ModAnalysis;
import dev.unifiedmc.mod.ModArtifact;
import dev.unifiedmc.mod.ModCompatibility;
import dev.unifiedmc.mod.ModCompatibilityStatus;
import dev.unifiedmc.mod.ModDependency;
import dev.unifiedmc.mod.ModDescriptor;
import dev.unifiedmc.mod.ModEnvironment;
import dev.unifiedmc.mod.ModId;
import dev.unifiedmc.mod.ModLoader;
import dev.unifiedmc.mod.ModMetadata;
import dev.unifiedmc.version.RuntimeVersion;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Metadata-first Mod Manager. It never loads a discovered JAR's bytecode. */
public final class DefaultModManager implements ModManager {
    private final ModMetadataReader metadataReader;
    private final ModCompatibilityCalculator compatibilityCalculator;
    private volatile ModConfig configuration;
    private volatile List<RuntimeVersion> runtimes;
    private volatile ModScanResult lastScan = emptyResult();

    public DefaultModManager(ModConfig configuration) {
        this(configuration, List.of());
    }

    public DefaultModManager(ModConfig configuration, List<RuntimeVersion> runtimes) {
        this(
                configuration,
                runtimes,
                new ArchiveMetadataReader(),
                new MetadataFirstCompatibilityCalculator());
    }

    public DefaultModManager(
            ModConfig configuration,
            List<RuntimeVersion> runtimes,
            ModMetadataReader metadataReader,
            ModCompatibilityCalculator compatibilityCalculator) {
        this.configuration = java.util.Objects.requireNonNull(configuration, "configuration");
        this.runtimes = List.copyOf(runtimes);
        this.metadataReader = java.util.Objects.requireNonNull(metadataReader, "metadataReader");
        this.compatibilityCalculator =
                java.util.Objects.requireNonNull(
                        compatibilityCalculator, "compatibilityCalculator");
    }

    public void configureRuntimes(List<RuntimeVersion> runtimes) {
        this.runtimes = List.copyOf(runtimes);
    }

    @Override
    public void configure(ModConfig configuration) {
        this.configuration = java.util.Objects.requireNonNull(configuration, "configuration");
    }

    @Override
    public List<ModDescriptor> list() {
        return lastScan.mods();
    }

    @Override
    public void refresh() {
        List<ModAnalysis> analyses = new ArrayList<>();
        List<ModDescriptor> descriptors = new ArrayList<>();
        List<ModDiagnostic> diagnostics = new ArrayList<>();
        Map<ModId, Set<ModId>> edges = new LinkedHashMap<>();
        Map<String, Path> hashes = new LinkedHashMap<>();
        Map<ModId, Path> ids = new LinkedHashMap<>();
        Set<ModId> duplicateIds = new LinkedHashSet<>();

        for (DirectorySource source : sources()) {
            Path directory = Path.of(source.path());
            try {
                Files.createDirectories(directory);
            } catch (IOException exception) {
                diagnostics.add(
                        error(
                                "DISCOVERY_DIRECTORY",
                                directory,
                                "directory",
                                "a readable managed mod directory",
                                exception.getMessage(),
                                "Check filesystem permissions and directory configuration."));
                continue;
            }
            try (var stream = Files.list(directory)) {
                stream.filter(DefaultModManager::isJar)
                        .sorted(Comparator.comparing(Path::toString))
                        .forEach(
                                path ->
                                        inspect(
                                                path,
                                                source.loader(),
                                                analyses,
                                                descriptors,
                                                diagnostics,
                                                hashes,
                                                ids,
                                                duplicateIds));
            } catch (IOException exception) {
                diagnostics.add(
                        error(
                                "DISCOVERY_SCAN",
                                directory,
                                "directory",
                                "a scannable managed mod directory",
                                exception.getMessage(),
                                "Check filesystem permissions and ensure the directory exists."));
            }
        }

        for (ModAnalysis analysis : analyses) {
            ModMetadata metadata = analysis.metadata();
            edges.computeIfAbsent(metadata.id(), ignored -> new LinkedHashSet<>());
            for (ModDependency dependency : metadata.dependencies()) {
                if (isPlatformDependency(dependency.modId())) {
                    continue;
                }
                Optional<ModMetadata> target =
                        analyses.stream()
                                .map(ModAnalysis::metadata)
                                .filter(m -> m.id().value().equals(dependency.modId()))
                                .findFirst();
                if (target.isEmpty()) {
                    ModDiagnostic diagnostic =
                            diagnostic(
                                    "MISSING_DEPENDENCY",
                                    dependency.required() || configuration.strictDependencies()
                                            ? "ERROR"
                                            : "WARNING",
                                    metadata.artifact().path(),
                                    "dependencies." + dependency.modId(),
                                    dependency.required()
                                            ? "Expected required dependency to be present"
                                            : "Optional dependency is not present",
                                    "missing",
                                    "Install "
                                            + dependency.modId()
                                            + " or disable this dependency.");
                    diagnostics.add(diagnostic);
                } else if (!ModVersionConstraints.matches(
                        target.get().version(), dependency.versionConstraint())) {
                    diagnostics.add(
                            diagnostic(
                                    "WRONG_DEPENDENCY_VERSION",
                                    dependency.required() || configuration.strictDependencies()
                                            ? "ERROR"
                                            : "WARNING",
                                    metadata.artifact().path(),
                                    "dependencies." + dependency.modId(),
                                    "Expected version matching " + dependency.versionConstraint(),
                                    target.get().version(),
                                    "Install a compatible version of " + dependency.modId() + "."));
                } else {
                    edges.computeIfAbsent(target.get().id(), ignored -> new LinkedHashSet<>())
                            .add(metadata.id());
                }
            }
        }

        ModGraph graph = new ModGraph(edges);
        try {
            graph.topologicalOrder();
        } catch (IllegalStateException cycle) {
            diagnostics.add(
                    error(
                            "DEPENDENCY_CYCLE",
                            Path.of("<mod-graph>"),
                            "dependencies",
                            "acyclic dependency graph",
                            graph.toString(),
                            "Remove or upgrade the mods that form the dependency cycle."));
        }

        List<ModDiagnostic> allDiagnostics = List.copyOf(diagnostics);
        boolean invalid =
                analyses.stream()
                                .anyMatch(
                                        a ->
                                                a.compatibility().status()
                                                        == ModCompatibilityStatus.INVALID)
                        || allDiagnostics.stream().anyMatch(d -> d.severity().equals("ERROR"));
        boolean duplicate =
                !duplicateIds.isEmpty()
                        || allDiagnostics.stream().anyMatch(d -> d.code().startsWith("DUPLICATE_"));
        ModScanResult.Status status;
        if (analyses.isEmpty() && !allDiagnostics.isEmpty()) {
            status = ModScanResult.Status.FAILED;
        } else if (duplicate) {
            status = ModScanResult.Status.DUPLICATE;
        } else if (invalid) {
            status = ModScanResult.Status.INVALID;
        } else if (analyses.isEmpty()) {
            status = ModScanResult.Status.EMPTY;
        } else {
            status = ModScanResult.Status.READY;
        }

        ModDiagnosticReport report =
                new ModDiagnosticReport(
                        "unifiedmc-mod-diagnostics-1", analyses, graph, allDiagnostics);
        lastScan =
                new ModScanResult(
                        status,
                        descriptors,
                        analyses,
                        graph,
                        report,
                        allDiagnostics.stream().map(ModDiagnostic::message).toList());
    }

    private void inspect(
            Path path,
            ModLoader directoryLoader,
            List<ModAnalysis> analyses,
            List<ModDescriptor> descriptors,
            List<ModDiagnostic> diagnostics,
            Map<String, Path> hashes,
            Map<ModId, Path> ids,
            Set<ModId> duplicateIds) {
        try {
            String hash = sha256(path);
            Path previous = hashes.putIfAbsent(hash, path);
            ModArtifact artifact = new ModArtifact(path, hash, directoryLoader, Files.size(path));
            if (previous != null && !previous.equals(path)) {
                diagnostics.add(
                        error(
                                "DUPLICATE_ARTIFACT",
                                path,
                                "sha256",
                                "unique artifact checksum",
                                hash,
                                "Remove the duplicate JAR or keep only one copy."));
            }
            List<String> entries = metadataReader.entries(artifact);
            List<ModMetadata> metadataList;
            try {
                metadataList = metadataReader.read(artifact, entries);
            } catch (Exception exception) {
                ModMetadata invalid = fallbackInvalid(artifact, exception.getMessage());
                metadataList = List.of(invalid);
                diagnostics.add(
                        error(
                                "INVALID_METADATA",
                                path,
                                "metadata",
                                "valid supported metadata",
                                exception.getClass().getSimpleName()
                                        + ": "
                                        + exception.getMessage(),
                                "Fix or replace the mod metadata descriptor."));
            }
            for (ModMetadata metadata : metadataList) {
                List<AnalysisStage> stages = new ArrayList<>();
                stages.add(AnalysisStage.DISCOVER);
                stages.add(AnalysisStage.IDENTIFY);
                stages.add(AnalysisStage.READ_METADATA);
                stages.add(AnalysisStage.DETERMINE_LOADER);
                stages.add(AnalysisStage.DETERMINE_MINECRAFT_VERSION);
                stages.add(AnalysisStage.DETERMINE_DEPENDENCIES);
                boolean loaderMismatch =
                        metadata.loader() != ModLoader.UNKNOWN
                                && metadata.loader() != directoryLoader;
                if (loaderMismatch) {
                    diagnostics.add(
                            error(
                                    "LOADER_MISMATCH",
                                    path,
                                    "loader",
                                    directoryLoader.name(),
                                    metadata.loader().name(),
                                    "Move the artifact into the matching loader directory or verify its metadata."));
                }
                Path priorId = ids.putIfAbsent(metadata.id(), path);
                if (priorId != null && !priorId.equals(path)) {
                    duplicateIds.add(metadata.id());
                    diagnostics.add(
                            new ModDiagnostic(
                                    "DUPLICATE_NAMESPACE",
                                    "ERROR",
                                    path.toString(),
                                    "id",
                                    "Duplicate mod id " + metadata.id().value(),
                                    "Keep only one artifact declaring this mod id."));
                }
                boolean environmentMismatch =
                        metadata.environment() == ModEnvironment.CLIENT
                                || metadata.environment() == ModEnvironment.UNKNOWN;
                if (environmentMismatch) {
                    diagnostics.add(
                            error(
                                    "INCOMPATIBLE_ENVIRONMENT",
                                    path,
                                    "environment",
                                    "server or both",
                                    metadata.environment().name(),
                                    "Install a dedicated-server-compatible build."));
                }
                ModCompatibility compatibility;
                if (loaderMismatch
                        || environmentMismatch
                        || metadata.loader() == ModLoader.UNKNOWN) {
                    compatibility =
                            new ModCompatibility(
                                    ModCompatibilityStatus.INVALID,
                                    Optional.empty(),
                                    List.of(
                                            loaderMismatch
                                                    ? "Loader directory does not match authoritative metadata"
                                                    : "Artifact metadata is not server-compatible"),
                                    List.of());
                } else {
                    stages.add(AnalysisStage.CALCULATE_COMPATIBILITY);
                    try {
                        compatibility = compatibilityCalculator.calculate(metadata, runtimes);
                    } catch (RuntimeException exception) {
                        compatibility =
                                new ModCompatibility(
                                        ModCompatibilityStatus.INVALID,
                                        Optional.empty(),
                                        List.of("Compatibility analysis failed"),
                                        List.of(exception.getMessage()));
                        diagnostics.add(
                                error(
                                        "COMPATIBILITY_ANALYSIS",
                                        path,
                                        "compatibility",
                                        "deterministic compatibility result",
                                        exception.getMessage(),
                                        "Inspect the mod metadata or add an explicit compatibility adapter."));
                    }
                }
                if (!runtimes.isEmpty()
                        && compatibility.status() == ModCompatibilityStatus.UNSUPPORTED) {
                    diagnostics.add(
                            diagnostic(
                                    "INCOMPATIBLE_VERSION",
                                    "WARNING",
                                    path,
                                    "minecraft",
                                    "one configured runtime",
                                    metadata.minecraftVersionConstraint(),
                                    "Install a mod build for one of the configured backend versions."));
                }
                stages.add(AnalysisStage.ASSIGN_RUNTIME);
                stages.add(AnalysisStage.APPROVE_REJECT);
                analyses.add(new ModAnalysis(metadata, compatibility, stages, List.of()));
                descriptors.add(
                        new ModDescriptor(
                                metadata.id(),
                                metadata.version(),
                                metadata.loader(),
                                metadata.minecraftVersion().map(v -> v.value()).orElse("unknown")));
            }
        } catch (Exception exception) {
            diagnostics.add(
                    error(
                            "INVALID_ARTIFACT",
                            path,
                            "artifact",
                            "valid readable JAR with supported metadata",
                            exception.getClass().getSimpleName() + ": " + exception.getMessage(),
                            "Replace the artifact with a valid mod JAR."));
            try {
                String hash = sha256(path);
                ModArtifact artifact =
                        new ModArtifact(path, hash, directoryLoader, Files.size(path));
                ModMetadata invalid = fallbackInvalid(artifact, exception.getMessage());
                Path priorId = ids.putIfAbsent(invalid.id(), path);
                if (priorId != null && !priorId.equals(path)) {
                    duplicateIds.add(invalid.id());
                    diagnostics.add(
                            new ModDiagnostic(
                                    "DUPLICATE_NAMESPACE",
                                    "ERROR",
                                    path.toString(),
                                    "id",
                                    "Duplicate mod id " + invalid.id().value(),
                                    "Keep only one artifact declaring this mod id."));
                }
                ModCompatibility compatibility =
                        new ModCompatibility(
                                ModCompatibilityStatus.INVALID,
                                Optional.empty(),
                                List.of("Artifact could not be analyzed safely"),
                                List.of(exception.getClass().getSimpleName()));
                analyses.add(
                        new ModAnalysis(
                                invalid,
                                compatibility,
                                List.of(
                                        AnalysisStage.DISCOVER,
                                        AnalysisStage.IDENTIFY,
                                        AnalysisStage.READ_METADATA,
                                        AnalysisStage.APPROVE_REJECT),
                                List.of(exception.getMessage())));
                descriptors.add(
                        new ModDescriptor(
                                invalid.id(), invalid.version(), invalid.loader(), "unknown"));
            } catch (Exception ignored) {
                // The primary INVALID_ARTIFACT diagnostic already records the failure.
            }
        }
    }

    private ModMetadata fallbackInvalid(ModArtifact artifact, String reason) {
        String file = artifact.path().getFileName().toString().replaceFirst("(?i)\\.jar$", "");
        String[] parts = file.split("[-_]");
        String id =
                parts.length > 0 && parts[0].matches("[a-z0-9][a-z0-9._-]*")
                        ? parts[0]
                        : "unknown-artifact";
        return new ModMetadata(
                artifact,
                new ModId(id),
                "UNKNOWN",
                id,
                ModLoader.UNKNOWN,
                ModEnvironment.UNKNOWN,
                Optional.empty(),
                "",
                "",
                List.of(),
                List.of(dev.unifiedmc.mod.ModCapability.UNKNOWN),
                "invalid:" + reason);
    }

    private static boolean isJar(Path path) {
        return Files.isRegularFile(path)
                && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar");
    }

    private static String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    if (read > 0) {
                        digest.update(buffer, 0, read);
                    }
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static ModDiagnostic error(
            String code, Path path, String property, String expected, String actual, String fix) {
        return diagnostic(
                code,
                "ERROR",
                path,
                property,
                "Expected " + expected + ", actual " + actual,
                actual,
                fix);
    }

    private static ModDiagnostic diagnostic(
            String code,
            String severity,
            Path path,
            String property,
            String message,
            String actual,
            String fix) {
        return new ModDiagnostic(
                code, severity, path.toString(), property, message + "; actual " + actual, fix);
    }

    private static boolean isPlatformDependency(String modId) {
        return switch (modId.toLowerCase(Locale.ROOT)) {
            case "minecraft", "fabricloader", "neoforge", "forge" -> true;
            default -> false;
        };
    }

    private List<DirectorySource> sources() {
        return List.of(
                new DirectorySource(configuration.fabricDirectory(), ModLoader.FABRIC),
                new DirectorySource(configuration.forgeDirectory(), ModLoader.FORGE),
                new DirectorySource(configuration.neoforgeDirectory(), ModLoader.NEOFORGE));
    }

    private static ModScanResult emptyResult() {
        ModGraph graph = new ModGraph(Map.of());
        return new ModScanResult(
                ModScanResult.Status.EMPTY,
                List.of(),
                List.of(),
                graph,
                new ModDiagnosticReport("unifiedmc-mod-diagnostics-1", List.of(), graph, List.of()),
                List.of("No scan has been performed"));
    }

    @Override
    public ModScanResult lastScan() {
        return lastScan;
    }

    @Override
    public ModConfig configuration() {
        return configuration;
    }

    private record DirectorySource(String path, ModLoader loader) {}
}
