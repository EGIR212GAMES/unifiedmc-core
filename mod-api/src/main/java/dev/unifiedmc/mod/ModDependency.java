package dev.unifiedmc.mod;

import java.util.Objects;

/** Declarative dependency extracted from mod metadata; it is never resolved by loading mod code. */
public record ModDependency(
        String modId, String versionConstraint, boolean required, ModEnvironment environment) {
    public ModDependency {
        Objects.requireNonNull(modId, "modId");
        Objects.requireNonNull(versionConstraint, "versionConstraint");
        Objects.requireNonNull(environment, "environment");
        if (modId.isBlank()) {

            throw new IllegalArgumentException("Dependency id must not be blank");
        }
    }
}
