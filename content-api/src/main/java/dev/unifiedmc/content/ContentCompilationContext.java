package dev.unifiedmc.content;

import dev.unifiedmc.uapi.compat.CompatibilityContext;
import java.nio.file.Path;
import java.util.Objects;

/** Backend-independent output context. */
public record ContentCompilationContext(
        CompatibilityContext compatibilityContext, Path outputDirectory) {
    public ContentCompilationContext {
        Objects.requireNonNull(compatibilityContext, "compatibilityContext");
        Objects.requireNonNull(outputDirectory, "outputDirectory");
    }
}
