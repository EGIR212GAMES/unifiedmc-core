package dev.unifiedmc.mod;

import java.util.Objects;

/** Stable logical mod identifier. */
public record ModId(String value) {
    public ModId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank() || !value.matches("[a-z0-9][a-z0-9._-]*")) {
            throw new IllegalArgumentException("Invalid mod id: " + value);
        }
    }
}
