package dev.unifiedmc.content.polymer;

import dev.unifiedmc.content.ContentCompileResult;
import dev.unifiedmc.content.ir.ContentDefinition;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Builds deterministic, backend-neutral resource artifacts for the Polymer backend. */
public final class PolymerResourceManager {
    public List<ContentCompileResult.EmittedArtifact> emit(
            List<? extends ContentDefinition> definitions) {
        List<ContentCompileResult.EmittedArtifact> artifacts = new ArrayList<>();
        definitions.stream()
                .sorted(Comparator.comparing(ContentDefinition::stableKey))
                .forEach(
                        definition ->
                                artifacts.add(
                                        new ContentCompileResult.EmittedArtifact(
                                                java.nio.file.Path.of(
                                                        "content",
                                                        definition.kind().name().toLowerCase(),
                                                        definition.id().namespace(),
                                                        definition.id().path() + ".json"),
                                                DeterministicJson.definition(definition)
                                                        .getBytes(StandardCharsets.UTF_8))));
        return List.copyOf(artifacts);
    }
}
