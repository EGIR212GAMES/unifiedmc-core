package dev.unifiedmc.runtime;

/** Lifecycle state of an isolated backend process. */
public enum BackendState {
    STOPPED,
    STARTING,
    RUNNING,
    STOPPING,
    FAILED
}
