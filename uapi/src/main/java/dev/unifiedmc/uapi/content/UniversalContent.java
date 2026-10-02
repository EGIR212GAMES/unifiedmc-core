package dev.unifiedmc.uapi.content;

import dev.unifiedmc.uapi.Capability;
import java.util.Map;
import java.util.Set;

/** Common semantic contract for content that can have multiple runtime representations. */
public interface UniversalContent {
    UniversalIdentifier id();

    Set<Capability> capabilities();

    Map<String, String> attributes();
}
