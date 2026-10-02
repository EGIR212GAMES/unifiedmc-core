package dev.unifiedmc.uapi.representation;

import dev.unifiedmc.uapi.content.UniversalIdentifier;
import java.util.Map;
import java.util.Objects;

/** Per-content representation map with independent Java/server/Bedrock targets. */
public record UniversalRepresentationBundle(
        Map<UniversalIdentifier, ContentRepresentations> representations) {
    public UniversalRepresentationBundle {
        Objects.requireNonNull(representations, "representations");
        representations.forEach(
                (id, value) -> {
                    Objects.requireNonNull(id, "representation id");
                    Objects.requireNonNull(value, "representation value");
                    value.javaRepresentation().ifPresent(r -> requireSameId(id, r.sourceId()));
                    value.serverSideRepresentation()
                            .ifPresent(r -> requireSameId(id, r.sourceId()));
                    value.bedrockRepresentation().ifPresent(r -> requireSameId(id, r.sourceId()));
                });
        representations = Map.copyOf(representations);
    }

    private static void requireSameId(
            dev.unifiedmc.uapi.content.UniversalIdentifier expected,
            dev.unifiedmc.uapi.content.UniversalIdentifier actual) {
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException(
                    "Representation source id "
                            + actual.value()
                            + " does not match map key "
                            + expected.value());
        }
    }

    public static UniversalRepresentationBundle empty() {
        return new UniversalRepresentationBundle(Map.of());
    }
}
