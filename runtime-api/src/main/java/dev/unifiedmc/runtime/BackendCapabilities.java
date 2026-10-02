package dev.unifiedmc.runtime;

import java.util.Set;

/** Declares capabilities exposed by a concrete backend without leaking runtime types. */
public record BackendCapabilities(Set<String> values) {
    public BackendCapabilities {
        values = Set.copyOf(values);
    }

    public boolean supports(String capability) {
        return values.contains(capability);
    }
}
