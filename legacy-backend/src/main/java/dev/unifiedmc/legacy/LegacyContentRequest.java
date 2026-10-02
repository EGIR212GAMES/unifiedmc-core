package dev.unifiedmc.legacy;

import java.util.Objects;

/** Runtime-neutral content translation request for explicit, known mappings only. */
public record LegacyContentRequest(String contentId, String sourceVersion, String targetVersion) {
    public LegacyContentRequest {
        Objects.requireNonNull(contentId, "contentId");
        Objects.requireNonNull(sourceVersion, "sourceVersion");
        Objects.requireNonNull(targetVersion, "targetVersion");
        if (contentId.isBlank()) {
            throw new IllegalArgumentException("contentId must not be blank");
        }
    }
}
