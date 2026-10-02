package dev.unifiedmc.api.health;

/** Mod discovery/validation state. */
public enum ModStatus {
    DISCOVERING,
    READY,
    DUPLICATE,
    FAILED,
    EMPTY
}
