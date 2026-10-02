package dev.unifiedmc.legacy;

import java.util.List;
import java.util.Objects;

/** Explicit legacy content translation result. */
public record LegacyContentTranslationResult(Status status, List<String> diagnostics) {
    public LegacyContentTranslationResult {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(diagnostics, "diagnostics");
        diagnostics = List.copyOf(diagnostics);
    }

    public enum Status {
        TRANSLATED,
        PARTIAL,
        UNSUPPORTED
    }
}
