package dev.unifiedmc.mod;

/** Final compatibility classification for a discovered logical mod. */
public enum ModCompatibilityStatus {
    SUPPORTED,
    SUPPORTED_VIA_ADAPTER,
    SUPPORTED_VIA_CONNECTOR,
    LEGACY_BACKEND_REQUIRED,
    UNSUPPORTED,
    INVALID
}
