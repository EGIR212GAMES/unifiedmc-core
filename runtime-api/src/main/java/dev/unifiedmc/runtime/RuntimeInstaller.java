package dev.unifiedmc.runtime;

/**
 * Trusted installer SPI. Implementations must use allowlisted sources and verify artifacts before
 * execution.
 */
public interface RuntimeInstaller {
    RuntimeInstallationResult install(RuntimeInstallationRequest request);
}
