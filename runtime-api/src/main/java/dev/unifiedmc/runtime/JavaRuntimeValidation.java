package dev.unifiedmc.runtime;

import java.util.Objects;

/** Immutable diagnostic for a Java/runtime compatibility check. */
public record JavaRuntimeValidation(boolean accepted, String diagnostic) {
    public JavaRuntimeValidation {
        Objects.requireNonNull(diagnostic, "diagnostic");
    }

    public static JavaRuntimeValidation accepted(String diagnostic) {
        return new JavaRuntimeValidation(true, diagnostic);
    }

    public static JavaRuntimeValidation rejected(String diagnostic) {
        return new JavaRuntimeValidation(false, diagnostic);
    }
}
