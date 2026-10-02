package dev.unifiedmc.mod;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Compatibility decision with explicit proof/source information and diagnostics. */
public record ModCompatibility(
        ModCompatibilityStatus status,
        Optional<String> assignedRuntime,
        List<String> reasons,
        List<String> diagnostics) {
    public ModCompatibility {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(assignedRuntime, "assignedRuntime");
        reasons = List.copyOf(reasons);
        diagnostics = List.copyOf(diagnostics);
    }
}
