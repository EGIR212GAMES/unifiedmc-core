package dev.unifiedmc.runtime;

import java.nio.file.Path;
import java.util.Objects;

/** Controlled installation request. No arbitrary URL is accepted by this contract. */
public record RuntimeInstallationRequest(
        String gameVersion, RuntimeBackend backend, Path runtimeRoot) {
    public RuntimeInstallationRequest {
        Objects.requireNonNull(gameVersion, "gameVersion");
        Objects.requireNonNull(backend, "backend");
        Objects.requireNonNull(runtimeRoot, "runtimeRoot");
    }
}
