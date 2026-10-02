package dev.unifiedmc.legacy;

/** Control-plane state published for one isolated legacy backend. */
public enum LegacyBackendStatus {
    DISCOVERING,
    READY,
    STARTING,
    RUNNING,
    STOPPING,
    FAILED,
    STOPPED
}
