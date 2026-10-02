package dev.unifiedmc.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Logging policy; values are not emitted by secret-bearing diagnostics. */
public record LoggingConfig(String level, boolean json, @JsonProperty("file") String file) {}
