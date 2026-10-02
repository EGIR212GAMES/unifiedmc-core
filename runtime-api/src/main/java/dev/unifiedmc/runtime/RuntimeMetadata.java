package dev.unifiedmc.runtime;

import dev.unifiedmc.version.JavaRuntimeRequirement;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/** Immutable metadata for one installed runtime without exposing loader implementation classes. */
public record RuntimeMetadata(
        String runtimeId,
        String gameVersion,
        RuntimeBackend backend,
        String loaderVersion,
        JavaRuntimeRequirement javaRequirement,
        Path rootDirectory,
        Path launchArtifact,
        boolean verified,
        String artifactSha256,
        String trustedSourceId) {
    public RuntimeMetadata {
        requireText(runtimeId, "runtimeId");
        requireText(gameVersion, "gameVersion");
        Objects.requireNonNull(backend, "backend");
        requireText(loaderVersion, "loaderVersion");
        Objects.requireNonNull(javaRequirement, "javaRequirement");
        Objects.requireNonNull(rootDirectory, "rootDirectory");
        Objects.requireNonNull(launchArtifact, "launchArtifact");
        requireText(artifactSha256, "artifactSha256");
        requireText(trustedSourceId, "trustedSourceId");
    }

    public Optional<Path> resolveLaunchArtifact() {
        Path normalizedRoot = rootDirectory.toAbsolutePath().normalize();
        Path normalizedArtifact = normalizedRoot.resolve(launchArtifact).normalize();
        return normalizedArtifact.startsWith(normalizedRoot)
                ? Optional.of(normalizedArtifact)
                : Optional.empty();
    }

    private static void requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
