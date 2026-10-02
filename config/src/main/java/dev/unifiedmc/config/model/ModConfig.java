package dev.unifiedmc.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Mod source and dependency policy. */
public record ModConfig(
        @JsonProperty("fabric-directory") String fabricDirectory,
        @JsonProperty("forge-directory") String forgeDirectory,
        @JsonProperty("neoforge-directory") String neoforgeDirectory,
        @JsonProperty("auto-download-dependencies") boolean autoDownloadDependencies,
        @JsonProperty("verify-signatures") boolean verifySignatures,
        @JsonProperty("strict-dependencies") boolean strictDependencies) {}
