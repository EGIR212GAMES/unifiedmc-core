package dev.unifiedmc.core;

import dev.unifiedmc.runtime.JavaRuntimeValidation;

/** Policy for the UnifiedMC Core process JVM. */
public final class CoreJavaPolicy {
    public static final int REQUIRED_JAVA = 25;

    private CoreJavaPolicy() {}

    public static JavaRuntimeValidation validate(int javaMajor) {
        if (javaMajor != REQUIRED_JAVA) {
            return JavaRuntimeValidation.rejected(
                    "UnifiedMC Core requires Java "
                            + REQUIRED_JAVA
                            + "; current JVM is Java "
                            + javaMajor);
        }
        return JavaRuntimeValidation.accepted("UnifiedMC Core is running on Java " + REQUIRED_JAVA);
    }
}
