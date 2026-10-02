package dev.unifiedmc.mod;

import java.util.List;
import java.util.Objects;

/** Complete metadata-first analysis result for one logical mod artifact. */
public record ModAnalysis(
        ModMetadata metadata,
        ModCompatibility compatibility,
        List<AnalysisStage> stages,
        List<String> diagnostics) {
    public ModAnalysis {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(compatibility, "compatibility");
        stages = List.copyOf(stages);
        diagnostics = List.copyOf(diagnostics);
    }
}
