package dev.unifiedmc.core;

/** Ordered phases of UnifiedMC startup orchestration. */
public enum LifecyclePhase {
    BOOTSTRAP,
    CONFIG_LOAD,
    ENVIRONMENT_CHECK,
    RUNTIME_DISCOVERY,
    MOD_DISCOVERY,
    DEPENDENCY_RESOLUTION,
    COMPATIBILITY_ANALYSIS,
    BACKEND_SELECTION,
    RESOURCE_PREPARATION,
    SERVER_START,
    READY
}
