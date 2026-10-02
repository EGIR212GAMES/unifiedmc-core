package dev.unifiedmc.runtime;

import dev.unifiedmc.version.JavaRuntimeRequirement;
import java.util.Objects;

/** Describes a backend runtime instance. */
public record BackendDescriptor(
        BackendId id,
        String gameVersion,
        String loader,
        String loaderVersion,
        JavaRuntimeRequirement javaRuntime) {
    public BackendDescriptor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(gameVersion, "gameVersion");
        Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(loaderVersion, "loaderVersion");
        Objects.requireNonNull(javaRuntime, "javaRuntime");
    }
}
