package dev.unifiedmc.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Security posture for managed artifacts and backend isolation. */
public record SecurityConfig(
        @JsonProperty("allow-unsigned-mods") boolean allowUnsignedMods,
        @JsonProperty("isolated-runtimes") boolean isolatedRuntimes,
        @JsonProperty("checksum-policy") ChecksumPolicy checksumPolicy) {}
