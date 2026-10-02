package dev.unifiedmc.core;

import java.util.List;

/** Explicit result for resource preparation. */
public record ResourcePreparationResult(Status status, List<String> diagnostics) {
    public ResourcePreparationResult {
        diagnostics = List.copyOf(diagnostics);
    }

    public enum Status {
        READY,
        NOT_REQUIRED,
        UNSUPPORTED,
        FAILED
    }
}
