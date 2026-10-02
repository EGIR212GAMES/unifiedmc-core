package dev.unifiedmc.legacy;

import java.util.Objects;

/** Controlled player-facing failure emitted when a legacy backend is unavailable. */
public record LegacyPlayerError(String backendId, String code, String message) {
    public LegacyPlayerError {
        Objects.requireNonNull(backendId, "backendId");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
    }
}
