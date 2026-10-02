package dev.unifiedmc.runtime;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Installation manifest persisted alongside a managed runtime. */
public record RuntimeInstallationManifest(
        String schema,
        String runtimeId,
        String gameVersion,
        RuntimeBackend backend,
        String loaderVersion,
        int minimumJava,
        int recommendedJava,
        String sourceId,
        boolean verified,
        String artifactSha256,
        String launchArtifact,
        List<String> launchArguments,
        Instant installedAt,
        Map<String, String> metadata) {
    public RuntimeInstallationManifest {
        Objects.requireNonNull(schema, "schema");
        Objects.requireNonNull(runtimeId, "runtimeId");
        Objects.requireNonNull(gameVersion, "gameVersion");
        Objects.requireNonNull(backend, "backend");
        Objects.requireNonNull(loaderVersion, "loaderVersion");
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(artifactSha256, "artifactSha256");
        Objects.requireNonNull(launchArtifact, "launchArtifact");
        Objects.requireNonNull(launchArguments, "launchArguments");
        Objects.requireNonNull(installedAt, "installedAt");
        Objects.requireNonNull(metadata, "metadata");
        launchArguments = List.copyOf(launchArguments);
        metadata = Map.copyOf(metadata);
        if (minimumJava < 8 || recommendedJava < minimumJava) {
            throw new IllegalArgumentException(
                    "Invalid Java runtime requirement in installation manifest");
        }
    }
}
