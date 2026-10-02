package dev.unifiedmc.content.polymer;

import dev.unifiedmc.content.ContentBackend;
import dev.unifiedmc.content.ContentBackendCapabilities;
import dev.unifiedmc.content.ContentCompilationContext;
import dev.unifiedmc.content.ContentCompileResult;
import dev.unifiedmc.content.compiler.ContentBackendCompiler;
import dev.unifiedmc.content.ir.ContentIrDocument;

/**
 * Polymer backend boundary. It compiles Content IR but does not claim a live Polymer runtime
 * integration.
 */
public final class PolymerBackend implements ContentBackend, ContentBackendCompiler {
    private final PolymerCompiler compiler;

    public PolymerBackend() {
        this(new PolymerCompiler());
    }

    public PolymerBackend(PolymerCompiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public String backendId() {
        return compiler.backendId();
    }

    @Override
    public ContentBackendCapabilities capabilities() {
        return compiler.capabilities();
    }

    @Override
    public ContentCompileResult compile(
            ContentIrDocument document, ContentCompilationContext context) {
        return compiler.compile(document, context);
    }
}
