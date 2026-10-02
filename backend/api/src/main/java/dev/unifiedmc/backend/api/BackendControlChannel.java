package dev.unifiedmc.backend.api;

/** Process control-plane contract; concrete transport is intentionally deferred. */
public interface BackendControlChannel {
    BackendHello hello();

    void shutdown();
}
