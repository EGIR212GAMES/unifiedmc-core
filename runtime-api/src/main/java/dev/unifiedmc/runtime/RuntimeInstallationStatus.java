package dev.unifiedmc.runtime;

/** Result classification for controlled runtime installation. */
public enum RuntimeInstallationStatus {
    INSTALLED,
    ALREADY_INSTALLED,
    UNSUPPORTED,
    REJECTED,
    FAILED;
}
