package dev.unifiedmc.create.profile;

/** Support boundary for one Create feature. No tier implies support. */
public enum CreateSupportTier {
    SERVER_NATIVE,
    SERVER_SIDE_EMULATABLE,
    POLYMER_REPRESENTABLE,
    BEDROCK_REPRESENTABLE,
    CLIENT_ONLY,
    UNSUPPORTED
}
