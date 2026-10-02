package dev.unifiedmc.content;

import java.util.Set;

/** Immutable capability declaration for one content compiler backend. */
public record ContentBackendCapabilities(Set<ContentProjectionCapability> supported) {
    public ContentBackendCapabilities {
        supported = Set.copyOf(supported);
    }

    public boolean supports(ContentProjectionCapability capability) {
        return supported.contains(capability);
    }
}
