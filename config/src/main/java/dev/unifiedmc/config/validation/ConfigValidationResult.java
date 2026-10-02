package dev.unifiedmc.config.validation;

import java.util.List;

/** Immutable validation result. */
public record ConfigValidationResult(List<ConfigDiagnostic> diagnostics) {
    public ConfigValidationResult {
        diagnostics = List.copyOf(diagnostics);
    }

    public boolean isValid() {
        return diagnostics.stream()
                .noneMatch(diagnostic -> diagnostic.severity() == ConfigDiagnostic.Severity.ERROR);
    }
}
