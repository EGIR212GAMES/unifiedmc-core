package dev.unifiedmc.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Backend/runtime selection settings. */
public record RuntimeConfig(
        @JsonProperty("primary-version") String primaryVersion,
        @JsonProperty("default-backend") String defaultBackend,
        @JsonProperty("allow-multi-runtime") boolean allowMultiRuntime,
        @JsonProperty("eula-accepted") boolean eulaAccepted) {
    public RuntimeConfig {
        if (primaryVersion == null || primaryVersion.isBlank()) {
            throw new IllegalArgumentException("primary-version must not be blank");
        }
        if (defaultBackend == null || defaultBackend.isBlank()) {
            throw new IllegalArgumentException("default-backend must not be blank");
        }
    }
}
