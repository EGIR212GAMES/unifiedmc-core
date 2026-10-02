package dev.unifiedmc.runtime;

/** Control-plane contract implemented by an isolated Minecraft backend. */
public interface UnifiedBackend {
    BackendDescriptor descriptor();

    RuntimeCapabilities capabilities();

    BackendState state();

    RuntimeHealth health();

    /** Starts the backend using the explicitly selected Java runtime. */
    void start(JavaRuntimeDescriptor javaRuntime, BackendLaunchRequest request);

    /** Stops the backend process. */
    void stop();
}
