package dev.unifiedmc.core;

/** Top-level state of the Core control-plane application. */
public enum CoreApplicationState {
    STOPPED,
    STARTING,
    READY,
    STOPPING,
    FAILED
}
