package dev.unifiedmc.uapi.representation.server;

import dev.unifiedmc.uapi.content.UniversalIdentifier;
import java.util.Map;

/** Server-side projection representation, independent of Java and Bedrock runtime APIs. */
public record ServerSideRepresentation(
        UniversalIdentifier sourceId,
        ProjectionKind projection,
        String vanillaTarget,
        Map<String, String> properties) {
    public ServerSideRepresentation {
        properties = Map.copyOf(properties);
    }

    public enum ProjectionKind {
        POLYMER,
        VANILLA_PACKET,
        SERVER_ONLY_LOGIC
    }
}
