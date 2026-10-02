package dev.unifiedmc.config;

import dev.unifiedmc.config.model.BedrockConfig;
import dev.unifiedmc.config.model.CompatibilityConfig;
import dev.unifiedmc.config.model.LoggingConfig;
import dev.unifiedmc.config.model.ModConfig;
import dev.unifiedmc.config.model.ProtocolConfig;
import dev.unifiedmc.config.model.RuntimeConfig;
import dev.unifiedmc.config.model.SecurityConfig;
import dev.unifiedmc.config.model.ServerConfig;
import dev.unifiedmc.config.model.StorageConfig;
import dev.unifiedmc.config.model.VersionConfig;

/** Fully typed UnifiedMC server configuration. */
public record CoreConfiguration(
        String schema,
        ServerConfig server,
        RuntimeConfig runtime,
        ModConfig mods,
        VersionConfig versions,
        CompatibilityConfig compatibility,
        BedrockConfig bedrock,
        ProtocolConfig protocol,
        SecurityConfig security,
        LoggingConfig logging,
        StorageConfig storage) {}
