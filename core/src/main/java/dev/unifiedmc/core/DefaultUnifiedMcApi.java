package dev.unifiedmc.core;

import dev.unifiedmc.api.UnifiedMcApi;

/** Minimal Core API implementation. */
public final class DefaultUnifiedMcApi implements UnifiedMcApi {
    @Override
    public int apiMajorVersion() {
        return 1;
    }
}
