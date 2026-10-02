package dev.unifiedmc.runtime;

import java.util.List;

/** Result of runtime validation. */
public record RuntimeValidationResult(boolean valid, List<String> errors, List<String> warnings) {

    public static RuntimeValidationResult valid(List<String> warnings) {
        return new RuntimeValidationResult(true, List.of(), warnings);
    }

    public static RuntimeValidationResult invalid(List<String> errors, List<String> warnings) {
        return new RuntimeValidationResult(false, List.copyOf(errors), List.copyOf(warnings));
    }
}
