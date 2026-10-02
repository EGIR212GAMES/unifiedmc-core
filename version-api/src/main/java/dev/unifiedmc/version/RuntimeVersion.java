package dev.unifiedmc.version;

import java.util.Objects;

/** Structured runtime version tuple kept separate from the game and Java runtime dimensions. */
public record RuntimeVersion(
        GameVersion game, String loader, String loaderVersion, JavaRuntimeRequirement javaRuntime) {
    public RuntimeVersion {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(loaderVersion, "loaderVersion");
        Objects.requireNonNull(javaRuntime, "javaRuntime");
    }
}
