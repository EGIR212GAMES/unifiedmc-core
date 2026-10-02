package dev.unifiedmc.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** Immutable, structured event emitted for each startup phase. */
public record LifecycleEvent(
        LifecyclePhase phase,
        LifecycleEventType type,
        Instant timestamp,
        Duration duration,
        String message) {
    public LifecycleEvent {
        Objects.requireNonNull(phase, "phase");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(duration, "duration");
        Objects.requireNonNull(message, "message");
    }
}
