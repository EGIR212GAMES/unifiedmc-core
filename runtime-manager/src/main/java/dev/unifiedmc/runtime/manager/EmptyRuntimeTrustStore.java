package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.RuntimeTrustStore;

/** Safe default: no artifact is executable until an installer registers a trusted checksum. */
public final class EmptyRuntimeTrustStore implements RuntimeTrustStore {
    @Override
    public boolean trusts(String sourceId, String sha256) {
        return false;
    }
}
