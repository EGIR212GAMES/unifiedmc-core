package dev.unifiedmc.config;

import java.nio.file.Path;

/** Retained only as an explicit guard against accidental alternate parser use. */
@Deprecated(forRemoval = true)
public final class UnsupportedConfigurationLoader implements ConfigurationLoader {
    @Override
    public CoreConfiguration load(Path path) {
        throw new UnsupportedOperationException(
                "Use TomlConfigurationLoader for UnifiedMC TOML configuration: " + path);
    }
}
