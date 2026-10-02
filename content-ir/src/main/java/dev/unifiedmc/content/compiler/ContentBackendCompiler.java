package dev.unifiedmc.content.compiler;

import dev.unifiedmc.content.ContentBackendCapabilities;
import dev.unifiedmc.content.ContentCompilationContext;
import dev.unifiedmc.content.ContentCompileResult;
import dev.unifiedmc.content.ir.ContentIrDocument;

public interface ContentBackendCompiler {
    String backendId();

    ContentBackendCapabilities capabilities();

    ContentCompileResult compile(ContentIrDocument document, ContentCompilationContext context);
}
