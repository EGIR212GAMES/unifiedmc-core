package dev.unifiedmc.runtime;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/** Point-in-time runtime health report. */
public record RuntimeHealth(
        RuntimeHealthStatus status,
        Instant checkedAt,
        long pid,
        String diagnostic,
        Optional<RuntimeCrashDiagnostics> crashDiagnostics) {
    public RuntimeHealth {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(checkedAt, "checkedAt");
        Objects.requireNonNull(diagnostic, "diagnostic");
        Objects.requireNonNull(crashDiagnostics, "crashDiagnostics");
    }
}
