package dev.unifiedmc.legacy;

import dev.unifiedmc.version.GameVersion;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/** Protocol-facing metadata for a legacy backend; packet translation remains a separate concern. */
public record LegacyProtocolProfile(
        GameVersion backendVersion,
        OptionalInt backendProtocol,
        List<String> acceptedClientVersions,
        String translationBoundary) {
    public LegacyProtocolProfile {
        Objects.requireNonNull(backendVersion, "backendVersion");
        Objects.requireNonNull(backendProtocol, "backendProtocol");
        Objects.requireNonNull(acceptedClientVersions, "acceptedClientVersions");
        Objects.requireNonNull(translationBoundary, "translationBoundary");
        acceptedClientVersions = List.copyOf(acceptedClientVersions);
        if (translationBoundary.isBlank()) {
            throw new IllegalArgumentException("translationBoundary must not be blank");
        }
    }
}
