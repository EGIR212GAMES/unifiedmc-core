package dev.unifiedmc.version;

import java.util.Objects;

/** Declares the Java requirement for one Minecraft version. */
public record JavaRuntimeRequirement(
        GameVersion minecraftVersion,
        int minimumJava,
        int recommendedJava,
        String sourceReference) {
    public JavaRuntimeRequirement {
        Objects.requireNonNull(minecraftVersion, "minecraftVersion");
        Objects.requireNonNull(sourceReference, "sourceReference");
        if (minimumJava < 8) {
            throw new IllegalArgumentException("minimumJava must be >= 8");
        }
        if (recommendedJava < minimumJava) {
            throw new IllegalArgumentException("recommendedJava must be >= minimumJava");
        }
    }

    public boolean accepts(int javaMajor) {
        if (minimumJava == recommendedJava) {
            return javaMajor == minimumJava;
        }
        return javaMajor >= minimumJava;
    }
}
