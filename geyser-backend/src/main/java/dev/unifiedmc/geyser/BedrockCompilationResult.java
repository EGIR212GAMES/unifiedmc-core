package dev.unifiedmc.geyser;

import dev.unifiedmc.content.ContentCompilationStatus;
import dev.unifiedmc.content.ContentCompileResult;
import dev.unifiedmc.content.ir.ContentIrDocument;
import java.util.List;

/** Result of explicit Bedrock compilation for a Content IR document. */
public record BedrockCompilationResult(
        ContentCompilationStatus status,
        ContentIrDocument ir,
        BedrockCapabilityReport capabilityReport,
        ContentCompileResult compileResult,
        List<BedrockDiagnostic> diagnostics) {
    public BedrockCompilationResult {
        diagnostics = List.copyOf(diagnostics);
    }
}
