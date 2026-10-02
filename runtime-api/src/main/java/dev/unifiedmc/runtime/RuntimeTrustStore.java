package dev.unifiedmc.runtime;

/**
 * External trust anchor for runtime artifacts; the installation manifest is not trusted by itself.
 */
public interface RuntimeTrustStore {
    boolean trusts(String sourceId, String sha256);
}
