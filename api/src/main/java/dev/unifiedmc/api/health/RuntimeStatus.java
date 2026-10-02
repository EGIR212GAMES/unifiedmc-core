package dev.unifiedmc.api.health;

/** Runtime lifecycle state exposed by health/status APIs. */
public enum RuntimeStatus {
    DISCOVERING,
    READY,
    STARTING,
    RUNNING,
    STOPPING,
    FAILED,
    STOPPED
}
