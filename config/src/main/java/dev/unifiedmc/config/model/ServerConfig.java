package dev.unifiedmc.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Public server endpoint and presentation settings. */
public record ServerConfig(
        String name,
        String motd,
        String bind,
        @JsonProperty("java-port") int javaPort,
        @JsonProperty("bedrock-port") int bedrockPort) {}
