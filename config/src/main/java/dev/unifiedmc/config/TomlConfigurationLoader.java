package dev.unifiedmc.config;

import java.nio.file.Path;

/** Production TOML configuration loader with validation and migration. */
public final class TomlConfigurationLoader implements ConfigurationLoader {
    private final ConfigurationService service;

    public TomlConfigurationLoader() {
        service = new ConfigurationService();
    }

    @Override
    public CoreConfiguration load(Path path) {
        return service.load(path);
    }
}
