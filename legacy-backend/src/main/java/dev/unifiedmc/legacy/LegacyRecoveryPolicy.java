package dev.unifiedmc.legacy;

import java.time.Duration;

/** Bounded crash-recovery policy for isolated legacy runtimes. */
public record LegacyRecoveryPolicy(boolean autoRestart, int maxRestarts, Duration restartDelay) {
    public LegacyRecoveryPolicy {
        if (maxRestarts < 0) {
            throw new IllegalArgumentException("maxRestarts must be >= 0");
        }
        if (restartDelay.isNegative()) {
            throw new IllegalArgumentException("restartDelay must not be negative");
        }
    }

    public static LegacyRecoveryPolicy disabled() {
        return new LegacyRecoveryPolicy(false, 0, Duration.ZERO);
    }

    public static LegacyRecoveryPolicy conservative() {
        return new LegacyRecoveryPolicy(true, 2, Duration.ofSeconds(2));
    }
}
