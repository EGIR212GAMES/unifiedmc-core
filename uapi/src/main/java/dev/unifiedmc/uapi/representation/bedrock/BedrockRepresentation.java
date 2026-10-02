package dev.unifiedmc.uapi.representation.bedrock;

import dev.unifiedmc.uapi.content.UniversalIdentifier;
import java.util.Map;

/** Bedrock representation. It is deliberately independent from Java/server-side objects. */
public record BedrockRepresentation(
        UniversalIdentifier sourceId,
        String identifier,
        String resourcePackId,
        String behaviorPackId,
        Map<String, String> properties) {
    public BedrockRepresentation {
        properties = Map.copyOf(properties);
    }
}
