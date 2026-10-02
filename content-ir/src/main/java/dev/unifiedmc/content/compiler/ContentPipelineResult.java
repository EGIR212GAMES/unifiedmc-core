package dev.unifiedmc.content.compiler;

import dev.unifiedmc.content.ContentCompileResult;
import dev.unifiedmc.content.ContentDiagnostic;
import dev.unifiedmc.content.ir.ContentIrDocument;
import java.util.List;

public record ContentPipelineResult(
        ContentCompileResult result,
        ContentIrDocument ir,
        List<StageEvent> stages,
        List<ContentDiagnostic> diagnostics) {
    public ContentPipelineResult {
        stages = List.copyOf(stages);
        diagnostics = List.copyOf(diagnostics);
    }

    public record StageEvent(ContentCompilerStage stage, StageStatus status, String detail) {}

    public enum StageStatus {
        COMPLETED,
        SKIPPED,
        FAILED
    }
}
