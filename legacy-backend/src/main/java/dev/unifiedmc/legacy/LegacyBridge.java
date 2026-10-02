package dev.unifiedmc.legacy;

/** Core-to-legacy bridge boundary. No Minecraft runtime object types cross this interface. */
public interface LegacyBridge {
    void statusChanged(String backendId, LegacyBackendStatus status, String diagnostic);

    void notifyPlayerError(LegacyPlayerError error);
}
