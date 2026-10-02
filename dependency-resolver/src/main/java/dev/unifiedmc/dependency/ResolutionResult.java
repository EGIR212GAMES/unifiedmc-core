package dev.unifiedmc.dependency;

import java.util.List;

/** Explicit dependency resolution result. */
public record ResolutionResult(Status status, List<String> diagnostics) {
    public ResolutionResult {
        diagnostics = List.copyOf(diagnostics);
    }

    public boolean successful() {
        return status == Status.RESOLVED;
    }

    public enum Status {
        RESOLVED,
        UNSUPPORTED,
        CONFLICT,
        INCOMPLETE,
        MISSING
    }
}
