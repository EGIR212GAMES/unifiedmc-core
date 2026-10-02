package dev.unifiedmc.version;

import java.util.Objects;

/**
 * Structured Minecraft game-version value; semantic ordering is intentionally not inferred here.
 */
public record GameVersion(String value) {
    public GameVersion {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Game version must not be blank");
        }
    }
}
