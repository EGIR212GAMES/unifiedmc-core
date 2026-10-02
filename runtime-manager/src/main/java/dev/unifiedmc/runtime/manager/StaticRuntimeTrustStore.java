package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.RuntimeTrustStore;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Deterministic trust store useful for tests and future packaged release metadata. */
public final class StaticRuntimeTrustStore implements RuntimeTrustStore {
    private final Set<String> trustedPairs;

    public StaticRuntimeTrustStore(Map<String, String> sourceToSha256) {
        Objects.requireNonNull(sourceToSha256, "sourceToSha256");
        this.trustedPairs =
                sourceToSha256.entrySet().stream()
                        .map(entry -> normalize(entry.getKey(), entry.getValue()))
                        .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean trusts(String sourceId, String sha256) {
        return trustedPairs.contains(normalize(sourceId, sha256));
    }

    private static String normalize(String sourceId, String sha256) {
        return sourceId.trim() + "#" + sha256.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
