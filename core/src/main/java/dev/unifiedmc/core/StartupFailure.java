package dev.unifiedmc.core;

import java.util.Objects;

/** Typed failure reported by startup orchestration. */
public final class StartupFailure extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final LifecyclePhase phase;
    private final FailureClass failureClass;

    public StartupFailure(
            LifecyclePhase phase, FailureClass failureClass, String message, Throwable cause) {
        super(message, cause);
        this.phase = Objects.requireNonNull(phase, "phase");
        this.failureClass = Objects.requireNonNull(failureClass, "failureClass");
    }

    public LifecyclePhase phase() {
        return phase;
    }

    public FailureClass failureClass() {
        return failureClass;
    }
}
