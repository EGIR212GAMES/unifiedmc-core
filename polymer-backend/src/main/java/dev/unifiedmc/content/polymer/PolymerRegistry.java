package dev.unifiedmc.content.polymer;

import dev.unifiedmc.content.ContentCompileResult;
import dev.unifiedmc.content.ir.ContentDefinition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Declarative registration plan for a future versioned Polymer runtime adapter. */
public final class PolymerRegistry {
    public List<ContentCompileResult.Registration> registrations(
            List<? extends ContentDefinition> definitions) {
        List<ContentCompileResult.Registration> entries = new ArrayList<>();
        definitions.stream()
                .sorted(Comparator.comparing(ContentDefinition::stableKey))
                .forEach(
                        definition ->
                                entries.add(
                                        new ContentCompileResult.Registration(
                                                definition.id().value(),
                                                definition.kind().name().toLowerCase(),
                                                Map.of(
                                                        "runtime",
                                                        "polymer",
                                                        "source",
                                                        "content-ir-v1"))));
        return List.copyOf(entries);
    }
}
