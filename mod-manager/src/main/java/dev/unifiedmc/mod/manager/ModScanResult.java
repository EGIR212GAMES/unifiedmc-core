package dev.unifiedmc.mod.manager;

import dev.unifiedmc.mod.ModAnalysis;
import dev.unifiedmc.mod.ModDescriptor;
import java.util.List;

/** Full metadata-first scan result. */
public record ModScanResult(
        Status status,
        List<ModDescriptor> mods,
        List<ModAnalysis> analyses,
        ModGraph graph,
        ModDiagnosticReport diagnosticsReport,
        List<String> diagnostics) {
    public ModScanResult {
        mods = List.copyOf(mods);
        analyses = List.copyOf(analyses);
        diagnostics = List.copyOf(diagnostics);
    }

    public ModScanResult(Status status, List<ModDescriptor> mods, List<String> diagnostics) {
        this(
                status,
                mods,
                List.of(),
                new ModGraph(java.util.Map.of()),
                new ModDiagnosticReport(
                        "unifiedmc-mod-diagnostics-1",
                        List.of(),
                        new ModGraph(java.util.Map.of()),
                        List.of()),
                diagnostics);
    }

    public enum Status {
        READY,
        EMPTY,
        DUPLICATE,
        FAILED,
        INVALID
    }
}
