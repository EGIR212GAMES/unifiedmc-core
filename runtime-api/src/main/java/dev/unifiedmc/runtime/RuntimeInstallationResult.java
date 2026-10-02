package dev.unifiedmc.runtime;

import java.util.List;

/** Diagnostic result of a controlled installation attempt. */
public record RuntimeInstallationResult(
        RuntimeInstallationStatus status, String message, List<String> diagnostics) {
    public RuntimeInstallationResult {
        diagnostics = List.copyOf(diagnostics);
    }
}
