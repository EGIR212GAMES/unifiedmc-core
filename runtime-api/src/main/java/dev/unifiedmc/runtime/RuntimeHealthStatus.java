package dev.unifiedmc.runtime;

/** Health state derived from process state and runtime diagnostics. */
public enum RuntimeHealthStatus {
    DISCOVERING,
    INSTALLING,
    STARTING,
    HEALTHY,
    STOPPING,
    STOPPED,
    FAILED,
    UNKNOWN;
}
