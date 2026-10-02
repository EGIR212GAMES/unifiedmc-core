package dev.unifiedmc.protocol;

/** Edge-gateway contract; actual Via* integration is intentionally deferred. */
public interface ProtocolGateway {
    boolean enabled();

    /** Throws until a concrete gateway implementation is installed. */
    void start();
}
