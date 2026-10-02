package dev.unifiedmc.runtime;

import java.util.Set;

/** Capabilities exposed by a runtime implementation. */
public record RuntimeCapabilities(Set<String> values) {
    public RuntimeCapabilities {
        values = Set.copyOf(values);
    }

    public boolean supports(String capability) {
        return values.contains(capability);
    }
}
