package dev.unifiedmc.bedrock;

import java.util.Set;

/** Explicit Geyser/ViaProxy integration boundary. */
public final class GeyserBackend {
    /** Declares this module without claiming a live Geyser runtime integration. */
    public Set<String> capabilities() {
        return Set.of("bridge-contract", "bedrock-capability-model");
    }

    public void start() {
        throw new UnsupportedOperationException(
                "Geyser integration is not implemented; use a concrete Geyser-ViaProxy adapter first");
    }
}
