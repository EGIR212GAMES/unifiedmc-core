package dev.unifiedmc.config;

import java.nio.file.Path;

/** Stable configuration loading boundary. */
public interface ConfigurationLoader {
    CoreConfiguration load(Path path);
}
