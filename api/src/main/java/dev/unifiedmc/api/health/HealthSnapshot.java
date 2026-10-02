package dev.unifiedmc.api.health;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** Typed point-in-time health snapshot for CLI and integrations. */
public record HealthSnapshot(
        HealthStatus overall,
        RuntimeStatus runtime,
        ModStatus mods,
        CompatibilityStatus compatibility,
        Map<String, CapabilityStatus> capabilities,
        Instant timestamp,
        String diagnostic) {
    public HealthSnapshot {
        Objects.requireNonNull(overall, "overall");
        Objects.requireNonNull(runtime, "runtime");
        Objects.requireNonNull(mods, "mods");
        Objects.requireNonNull(compatibility, "compatibility");
        capabilities = Map.copyOf(capabilities);
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(diagnostic, "diagnostic");
    }
}
