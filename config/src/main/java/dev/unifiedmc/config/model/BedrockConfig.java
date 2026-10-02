package dev.unifiedmc.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Geyser/Bedrock integration flags. */
public record BedrockConfig(
        boolean enabled,
        boolean geyser,
        @JsonProperty("custom-content") boolean customContent,
        @JsonProperty("resource-packs") boolean resourcePacks) {}
