package dev.unifiedmc.mod;

/** Metadata-observable capabilities; these do not imply runtime compatibility. */
public enum ModCapability {
    MAIN_ENTRYPOINT,
    SERVER_ENTRYPOINT,
    CLIENT_ENTRYPOINT,
    MIXIN,
    ACCESS_WIDENER,
    CUSTOM_NETWORK,
    RESOURCE_PACK,
    DATA_PACK,
    WORLDGEN,
    UNKNOWN
}
