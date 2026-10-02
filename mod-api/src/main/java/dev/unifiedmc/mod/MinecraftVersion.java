package dev.unifiedmc.mod;

import java.util.Objects;

/** Minecraft version value extracted from authoritative mod metadata when possible. */
public record MinecraftVersion(String value) {
    public MinecraftVersion {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {

            throw new IllegalArgumentException("Minecraft version must not be blank");
        }
    }
}
