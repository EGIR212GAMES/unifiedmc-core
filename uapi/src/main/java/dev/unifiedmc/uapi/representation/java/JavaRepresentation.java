package dev.unifiedmc.uapi.representation.java;

import dev.unifiedmc.uapi.content.UniversalIdentifier;
import java.util.Map;

/** Java-client/runtime representation. It intentionally contains no Minecraft classes. */
public record JavaRepresentation(
        UniversalIdentifier sourceId,
        String registryType,
        String targetIdentifier,
        Map<String, String> properties) {
    public JavaRepresentation {
        properties = Map.copyOf(properties);
    }
}
