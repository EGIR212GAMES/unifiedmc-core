package dev.unifiedmc.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Filesystem locations owned by the server installation. */
public record StorageConfig(
        @JsonProperty("backend-directory") String backendDirectory,
        @JsonProperty("pack-directory") String packDirectory,
        @JsonProperty("runtime-directory") String runtimeDirectory) {}
