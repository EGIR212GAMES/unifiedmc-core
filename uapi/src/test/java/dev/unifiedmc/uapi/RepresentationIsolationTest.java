package dev.unifiedmc.uapi;

import static org.junit.jupiter.api.Assertions.*;

import dev.unifiedmc.uapi.content.UniversalIdentifier;
import dev.unifiedmc.uapi.representation.ContentRepresentations;
import dev.unifiedmc.uapi.representation.bedrock.BedrockRepresentation;
import dev.unifiedmc.uapi.representation.java.JavaRepresentation;
import dev.unifiedmc.uapi.representation.server.ServerSideRepresentation;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RepresentationIsolationTest {
    @Test
    void representationsRemainSeparateTypedContracts() {
        UniversalIdentifier id = new UniversalIdentifier("example", "thing");
        ContentRepresentations bundle =
                new ContentRepresentations(
                        Optional.of(new JavaRepresentation(id, "item", id.value(), Map.of())),
                        Optional.of(
                                new ServerSideRepresentation(
                                        id,
                                        ServerSideRepresentation.ProjectionKind.POLYMER,
                                        "minecraft:stick",
                                        Map.of())),
                        Optional.of(
                                new BedrockRepresentation(
                                        id, "example:thing", "pack", "behavior", Map.of())));

        assertTrue(bundle.javaRepresentation().isPresent());
        assertTrue(bundle.serverSideRepresentation().isPresent());
        assertTrue(bundle.bedrockRepresentation().isPresent());
        assertEquals(
                "minecraft:stick", bundle.serverSideRepresentation().orElseThrow().vanillaTarget());
        assertEquals("example:thing", bundle.bedrockRepresentation().orElseThrow().identifier());
    }
}
