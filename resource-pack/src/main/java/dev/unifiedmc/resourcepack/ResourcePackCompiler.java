package dev.unifiedmc.resourcepack;

/** Dual-target resource pack compiler contract. */
public interface ResourcePackCompiler {
    CompileTarget target();

    void compile();

    enum CompileTarget {
        JAVA,
        BEDROCK
    }
}
