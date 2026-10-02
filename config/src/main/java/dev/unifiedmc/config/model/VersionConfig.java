package dev.unifiedmc.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Client and backend version allow-list settings. */
public record VersionConfig(
        @JsonProperty("allowed-client-versions") List<String> allowedClientVersions,
        @JsonProperty("backend-versions") List<String> backendVersions) {
    public VersionConfig {
        allowedClientVersions = List.copyOf(allowedClientVersions);
        backendVersions = List.copyOf(backendVersions);
    }
}
