package dev.unifiedmc.legacy;

/** Safe default bridge that records no network-side actions; concrete gateways can replace it. */
public final class NoopLegacyBridge implements LegacyBridge {
    @Override
    public void statusChanged(String backendId, LegacyBackendStatus status, String diagnostic) {
        // Intentionally no-op: this module does not own the external player gateway.
    }

    @Override
    public void notifyPlayerError(LegacyPlayerError error) {
        // Intentionally no-op: player messaging belongs to the Core gateway.
    }
}
