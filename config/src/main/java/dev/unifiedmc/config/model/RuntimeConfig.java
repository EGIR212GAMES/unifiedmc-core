package dev.unifiedmc.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Backend/runtime selection settings. */
public record RuntimeConfig(
        @JsonProperty("primary-version") String primaryVersion,
        @JsonProperty("default-backend") String defaultBackend,
        @JsonProperty("allow-multi-runtime") boolean allowMultiRuntime) {}
