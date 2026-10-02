package dev.unifiedmc.mod.manager;

import dev.unifiedmc.mod.ModAnalysis;
import java.util.List;

/** JSON-serializable diagnostics document for GUI/launcher consumers. */
public record ModDiagnosticReport(
        String schema,
        List<ModAnalysis> analyses,
        ModGraph graph,
        List<ModDiagnostic> diagnostics) {
    public ModDiagnosticReport {
        analyses = List.copyOf(analyses);
        diagnostics = List.copyOf(diagnostics);
    }
}
