package dev.unifiedmc.runtime;

/** Supported runtime backend families. Concrete backend integrations are added independently. */
public enum RuntimeBackend {
    MODERN_NEOFORGE,
    FABRIC_VIA_CONNECTOR,
    LEGACY_FORGE,
    FUTURE_CUSTOM;
}
