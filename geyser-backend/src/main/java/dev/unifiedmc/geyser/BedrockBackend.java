package dev.unifiedmc.geyser;

import dev.unifiedmc.content.ContentBackend;
import dev.unifiedmc.content.ContentBackendCapabilities;
import dev.unifiedmc.content.ContentCompilationContext;
import dev.unifiedmc.content.ContentCompileResult;
import dev.unifiedmc.content.ContentProjectionCapability;
import dev.unifiedmc.content.compiler.ContentBackendCompiler;
import dev.unifiedmc.content.ir.ContentIrDocument;
import dev.unifiedmc.geyser.compiler.BedrockCompiler;
import java.util.EnumSet;

/** Explicit Bedrock backend. It emits Geyser-consumable mappings and a Bedrock pack. */
public final class BedrockBackend implements ContentBackend, ContentBackendCompiler {
    private final BedrockCompiler compiler;

    public BedrockBackend() {
        this(new BedrockCompiler());
    }

    public BedrockBackend(BedrockCompiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public String backendId() {
        return "geyser-bedrock";
    }

    @Override
    public ContentBackendCapabilities capabilities() {
        return new ContentBackendCapabilities(
                EnumSet.of(
                        ContentProjectionCapability.BLOCKS,
                        ContentProjectionCapability.ITEMS,
                        ContentProjectionCapability.ENTITIES,
                        ContentProjectionCapability.RECIPES,
                        ContentProjectionCapability.RESOURCE_PACKS));
    }

    @Override
    public ContentCompileResult compile(
            ContentIrDocument document, ContentCompilationContext context) {
        return compiler.compile(document, context);
    }

    public BedrockCompilationResult compileDetailed(
            ContentIrDocument document, ContentCompilationContext context) {
        return compiler.compileDetailed(document, context);
    }
}
