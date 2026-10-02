package dev.unifiedmc.legacy;

import dev.unifiedmc.runtime.RuntimeInstallationManifest;
import dev.unifiedmc.runtime.RuntimeMetadata;
import dev.unifiedmc.runtime.RuntimeValidator;
import dev.unifiedmc.runtime.manager.AbstractProcessRuntime;

/** Minecraft/Forge runtime wrapper for an isolated legacy process. */
public final class LegacyRuntime extends AbstractProcessRuntime {
    private final String backendId;

    public LegacyRuntime(
            String backendId,
            RuntimeMetadata metadata,
            RuntimeInstallationManifest manifest,
            RuntimeValidator validator) {
        super(metadata, manifest, validator);
        this.backendId = backendId;
    }

    public String backendId() {
        return backendId;
    }
}
