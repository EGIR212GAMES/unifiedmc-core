package dev.unifiedmc.protocol;

import java.util.Objects;

/** Opaque protocol identifier kept separate from Minecraft game version. */
public record ProtocolVersion(String value) {
    public ProtocolVersion {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Protocol version must not be blank");
        }
    }
}
