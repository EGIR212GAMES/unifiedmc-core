package dev.unifiedmc.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Feature flags for compatibility projections and adapters. */
public record CompatibilityConfig(
        boolean enabled,
        CompatibilityMode mode,
        @JsonProperty("fail-on-unsupported") boolean failOnUnsupported,
        @JsonProperty("allow-partial-support") boolean allowPartialSupport) {}
