package dev.unifiedmc.uapi.representation;

import dev.unifiedmc.uapi.representation.bedrock.BedrockRepresentation;
import dev.unifiedmc.uapi.representation.java.JavaRepresentation;
import dev.unifiedmc.uapi.representation.server.ServerSideRepresentation;
import java.util.Objects;
import java.util.Optional;

/** Separates Java, server-side and Bedrock content representations. */
public record ContentRepresentations(
        Optional<JavaRepresentation> javaRepresentation,
        Optional<ServerSideRepresentation> serverSideRepresentation,
        Optional<BedrockRepresentation> bedrockRepresentation) {
    public ContentRepresentations {
        javaRepresentation = Objects.requireNonNull(javaRepresentation, "javaRepresentation");
        serverSideRepresentation =
                Objects.requireNonNull(serverSideRepresentation, "serverSideRepresentation");
        bedrockRepresentation =
                Objects.requireNonNull(bedrockRepresentation, "bedrockRepresentation");
    }

    public static ContentRepresentations none() {
        return new ContentRepresentations(Optional.empty(), Optional.empty(), Optional.empty());
    }
}
