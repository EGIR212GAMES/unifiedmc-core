package dev.unifiedmc.runtime;

/** Installed runtime instance capable of being started in isolation. */
public interface RuntimeInstance {
    RuntimeMetadata metadata();

    RuntimeCapabilities capabilities();

    RuntimeHealth health();

    RuntimeHandle start(RuntimeRequest request);
}
