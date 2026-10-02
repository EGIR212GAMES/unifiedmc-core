package dev.unifiedmc.runtime;

import java.util.List;

/** Validation outcome for an installed runtime. */
public record RuntimeValidationResult(boolean valid, List<String> errors, List<String> warnings) {
    public RuntimeValidationResult {
        errors = List.copyOf(errors);
        warnings = List.copyOf(warnings);
    }

    public static RuntimeValidationResult valid(List<String> warnings) {
        return new RuntimeValidationResult(true, List.of(), warnings);
    }

    public static RuntimeValidationResult invalid(List<String> errors, List<String> warnings) {
        return new RuntimeValidationResult(false, errors, warnings);
    }
}
