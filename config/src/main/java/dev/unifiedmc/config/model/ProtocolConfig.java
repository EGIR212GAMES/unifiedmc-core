package dev.unifiedmc.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Protocol gateway bounds. */
public record ProtocolConfig(
        boolean enabled,
        @JsonProperty("minimum-client") String minimumClient,
        @JsonProperty("maximum-client") String maximumClient) {}
