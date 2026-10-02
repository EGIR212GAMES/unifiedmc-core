package dev.unifiedmc.config;

import dev.unifiedmc.config.model.BedrockConfig;
import dev.unifiedmc.config.model.ChecksumPolicy;
import dev.unifiedmc.config.model.CompatibilityConfig;
import dev.unifiedmc.config.model.CompatibilityMode;
import dev.unifiedmc.config.model.LoggingConfig;
import dev.unifiedmc.config.model.ModConfig;
import dev.unifiedmc.config.model.ProtocolConfig;
import dev.unifiedmc.config.model.RuntimeConfig;
import dev.unifiedmc.config.model.SecurityConfig;
import dev.unifiedmc.config.model.ServerConfig;
import dev.unifiedmc.config.model.StorageConfig;
import dev.unifiedmc.config.model.VersionConfig;
import java.util.List;

/** Supplies a safe, explicit default configuration. */
public final class DefaultConfigurationFactory {
    public static final String DEFAULT_SERVER_NAME = "UnifiedMC";
    public static final String DEFAULT_SCHEMA = "unifiedmc-2";

    private DefaultConfigurationFactory() {}

    public static CoreConfiguration create() {
        return new CoreConfiguration(
                DEFAULT_SCHEMA,
                new ServerConfig(DEFAULT_SERVER_NAME, "UnifiedMC Server", "0.0.0.0", 25565, 19132),
                new RuntimeConfig("26.3", "neoforge-26.3", false, false),
                new ModConfig("./FabricMods", "./ForgeMods", "./NeoForgeMods", false, true, true),
                new VersionConfig(List.of("1.21.1", "26.1", "26.2", "26.3"), List.of("26.3")),
                new CompatibilityConfig(true, CompatibilityMode.STRICT, true, false),
                new BedrockConfig(true, true, false, true),
                new ProtocolConfig(true, "1.21.1", "26.3"),
                new SecurityConfig(false, true, ChecksumPolicy.REQUIRED),
                new LoggingConfig("INFO", false, "./logs/unifiedmc.log"),
                new StorageConfig("./backends", "./packs", "./runtimes"));
    }
}
