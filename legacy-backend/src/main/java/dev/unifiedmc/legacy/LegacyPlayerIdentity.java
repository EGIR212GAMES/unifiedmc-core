package dev.unifiedmc.legacy;

import java.util.Objects;
import java.util.UUID;

/** Core-owned player identity presented to a legacy backend bridge. */
public record LegacyPlayerIdentity(UUID id, String name) {
    public LegacyPlayerIdentity {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
