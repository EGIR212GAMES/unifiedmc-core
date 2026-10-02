package dev.unifiedmc.uapi.compat;

import dev.unifiedmc.uapi.Capability;
import java.util.List;
import java.util.Set;

/** Explicit compatibility result. Partial support is never collapsed into supported. */
public record CompatibilityReport(
        Status status,
        String adapterId,
        Set<Capability> supportedCapabilities,
        Set<Capability> unsupportedCapabilities,
        List<String> unsupportedFeatures,
        List<String> limitations) {
    public CompatibilityReport {
        supportedCapabilities = Set.copyOf(supportedCapabilities);
        unsupportedCapabilities = Set.copyOf(unsupportedCapabilities);
        unsupportedFeatures = List.copyOf(unsupportedFeatures);
        limitations = List.copyOf(limitations);
    }

    public boolean supported() {
        return status == Status.SUPPORTED;
    }

    public enum Status {
        SUPPORTED,
        PARTIAL,
        UNSUPPORTED
    }
}
