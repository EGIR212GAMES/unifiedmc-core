package dev.unifiedmc.mod;

/** Ordered stages of metadata-first mod inspection. */
public enum AnalysisStage {
    DISCOVER,
    IDENTIFY,
    READ_METADATA,
    DETERMINE_LOADER,
    DETERMINE_MINECRAFT_VERSION,
    DETERMINE_DEPENDENCIES,
    CALCULATE_COMPATIBILITY,
    ASSIGN_RUNTIME,
    APPROVE_REJECT
}
