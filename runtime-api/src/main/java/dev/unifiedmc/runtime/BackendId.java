package dev.unifiedmc.runtime;

import java.util.Objects;

/** Stable identifier for a managed backend instance. */
public record BackendId(String value) {
    public BackendId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Backend id must not be blank");
        }
    }
}
