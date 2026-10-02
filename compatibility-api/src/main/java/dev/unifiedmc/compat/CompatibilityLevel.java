package dev.unifiedmc.compat;

/** Explicit compatibility result categories. */
public enum CompatibilityLevel {
    NATIVE,
    PROTOCOL_ONLY,
    SERVER_SIDE_PROJECTED,
    ADAPTER_BACKED,
    PARTIAL,
    UNSUPPORTED
}
