package dev.unifiedmc.core;

/** Stable classification for startup/shutdown failures. */
public enum FailureClass {
    CONFIGURATION,
    ENVIRONMENT,
    RUNTIME_DISCOVERY,
    MOD_DISCOVERY,
    DEPENDENCY,
    COMPATIBILITY,
    BACKEND_SELECTION,
    RESOURCE_PREPARATION,
    BACKEND_STARTUP,
    SHUTDOWN,
    INTERNAL
}
