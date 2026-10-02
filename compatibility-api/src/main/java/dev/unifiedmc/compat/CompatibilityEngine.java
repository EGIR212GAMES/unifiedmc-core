package dev.unifiedmc.compat;

/** Compatibility decision boundary. It must never promise unsupported translation. */
public interface CompatibilityEngine {
    CompatibilityResult evaluate(CompatibilityRequest request);
}
