package dev.unifiedmc.api.health;

/** Compatibility analysis state. */
public enum CompatibilityStatus {
    DISABLED,
    ANALYZING,
    READY,
    PARTIAL,
    UNSUPPORTED,
    FAILED
}
