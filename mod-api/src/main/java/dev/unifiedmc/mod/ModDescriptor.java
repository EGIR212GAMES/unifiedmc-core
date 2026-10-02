package dev.unifiedmc.mod;

import java.util.Objects;

/** Backward-compatible compact view used by older core lifecycle contracts. */
public record ModDescriptor(ModId id, String version, ModLoader loader, String gameVersion) {
    public ModDescriptor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(gameVersion, "gameVersion");
    }

    public ModDescriptor(ModId id, String version, ModLoaderType loader, String gameVersion) {
        this(id, version, ModLoader.valueOf(loader.name()), gameVersion);
    }

    public ModLoaderType loaderType() {
        return ModLoaderType.valueOf(loader.name());
    }
}
