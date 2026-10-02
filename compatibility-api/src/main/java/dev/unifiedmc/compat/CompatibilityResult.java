package dev.unifiedmc.compat;

import java.util.List;

/** Deterministic compatibility report; unsupported work is never silently downgraded. */
public record CompatibilityResult(CompatibilityLevel level, List<String> diagnostics) {
    public CompatibilityResult {
        diagnostics = List.copyOf(diagnostics);
    }
}
