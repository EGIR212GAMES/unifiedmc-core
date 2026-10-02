package dev.unifiedmc.resourcepack;

/** Explicitly unsupported compiler until pack transformation rules are implemented. */
public final class UnsupportedResourcePackCompiler implements ResourcePackCompiler {
    private final CompileTarget target;

    public UnsupportedResourcePackCompiler(CompileTarget target) {
        this.target = target;
    }

    @Override
    public CompileTarget target() {
        return target;
    }

    @Override
    public void compile() {
        throw new UnsupportedOperationException("Resource pack compilation is not implemented yet");
    }
}
