package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.RuntimeBackend;
import dev.unifiedmc.version.RuntimeJavaCompatibility;
import java.util.List;

/** Architectural runtime registry without pinning backend build numbers. */
public final class DefaultRuntimeCatalog {
    private final RuntimeJavaCompatibility javaCompatibility;

    public DefaultRuntimeCatalog() {
        javaCompatibility = RuntimeJavaCompatibility.officialBaseline();
    }

    public List<RuntimeCatalogEntry> entries() {
        return List.of(
                entry("26.3", RuntimeBackend.MODERN_NEOFORGE),
                entry("26.2", RuntimeBackend.MODERN_NEOFORGE),
                entry("26.1", RuntimeBackend.MODERN_NEOFORGE),
                entry("1.21.1", RuntimeBackend.MODERN_NEOFORGE),
                entry("1.20.1", RuntimeBackend.MODERN_NEOFORGE),
                entry("1.20.1", RuntimeBackend.LEGACY_FORGE),
                entry("1.18.2", RuntimeBackend.LEGACY_FORGE),
                entry("1.12.2", RuntimeBackend.LEGACY_FORGE),
                entry("1.7.10", RuntimeBackend.LEGACY_FORGE));
    }

    private RuntimeCatalogEntry entry(String gameVersion, RuntimeBackend backend) {
        return new RuntimeCatalogEntry(
                gameVersion, backend, javaCompatibility.requirementFor(gameVersion));
    }
}
