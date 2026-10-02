package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.JavaRuntimeDescriptor;
import dev.unifiedmc.runtime.RuntimeInstallationManifest;
import dev.unifiedmc.runtime.RuntimeTrustStore;
import dev.unifiedmc.runtime.RuntimeValidationResult;
import dev.unifiedmc.version.GameVersion;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Validates manifests, artifact integrity, trusted source identity and selected Java runtime. */
public final class RuntimeValidator {
    private static final String EXPECTED_SCHEMA = "unifiedmc-runtime-1";
    private static final Set<String> TRUSTED_SOURCES =
            Set.of(
                    "official:minecraft",
                    "official:neoforge",
                    "official:forge",
                    "official:fabric",
                    "local:operator-approved");
    private final RuntimeTrustStore trustStore;

    public RuntimeValidator() {
        this(new EmptyRuntimeTrustStore());
    }

    public RuntimeValidator(RuntimeTrustStore trustStore) {
        this.trustStore = java.util.Objects.requireNonNull(trustStore, "trustStore");
    }

    public RuntimeValidationResult validate(
            Path root, RuntimeInstallationManifest manifest, JavaRuntimeDescriptor javaRuntime) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        if (!EXPECTED_SCHEMA.equals(manifest.schema())) {
            errors.add("Unsupported runtime manifest schema: " + manifest.schema());
        }
        if (!TRUSTED_SOURCES.contains(manifest.sourceId())) {
            errors.add("Runtime source is not trusted: " + manifest.sourceId());
        }
        if (!manifest.verified()) {
            errors.add("Runtime installation is not marked verified");
        }
        if (!trustStore.trusts(manifest.sourceId(), manifest.artifactSha256())) {
            errors.add(
                    "Artifact checksum is not present in the runtime trust store for source: "
                            + manifest.sourceId());
        }
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path artifact = normalizedRoot.resolve(manifest.launchArtifact()).normalize();
        if (!artifact.startsWith(normalizedRoot)) {
            errors.add("Launch artifact escapes runtime root: " + manifest.launchArtifact());
        } else if (!Files.isRegularFile(artifact)) {
            errors.add("Launch artifact is missing: " + artifact);
        } else {
            try {
                String actual = RuntimeHashing.sha256(artifact);
                if (!actual.equalsIgnoreCase(manifest.artifactSha256())) {
                    errors.add(
                            "Launch artifact checksum mismatch: expected "
                                    + manifest.artifactSha256()
                                    + ", actual "
                                    + actual);
                }
            } catch (IOException exception) {
                errors.add(
                        "Could not calculate runtime artifact checksum: " + exception.getMessage());
            }
        }
        JavaRuntimeRequirement requirement =
                new JavaRuntimeRequirement(
                        new GameVersion(manifest.gameVersion()),
                        manifest.minimumJava(),
                        manifest.recommendedJava(),
                        "runtime manifest");
        if (javaRuntime == null) {
            warnings.add("No Java runtime selected; runtime can be discovered but not started");
        } else if (!requirement.accepts(javaRuntime.javaVersion())) {
            errors.add(
                    "Selected Java "
                            + javaRuntime.javaVersion()
                            + " does not satisfy Minecraft "
                            + manifest.gameVersion()
                            + " (required Java "
                            + manifest.minimumJava()
                            + ")");
        }
        if (manifest.launchArguments().stream().anyMatch(argument -> argument.contains("://"))) {
            errors.add("Launch arguments must not contain URL schemes");
        }
        return errors.isEmpty()
                ? RuntimeValidationResult.valid(warnings)
                : RuntimeValidationResult.invalid(errors, warnings);
    }

    public RuntimeValidationResult validateManifestOnly(
            Path root, RuntimeInstallationManifest manifest) {
        return validate(root, manifest, null);
    }

    public boolean isTrustedSource(String sourceId) {
        return TRUSTED_SOURCES.contains(sourceId);
    }
}
