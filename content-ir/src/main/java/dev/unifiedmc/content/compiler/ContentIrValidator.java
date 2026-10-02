package dev.unifiedmc.content.compiler;

import dev.unifiedmc.content.ContentDiagnostic;
import dev.unifiedmc.content.ir.ContentDefinition;
import dev.unifiedmc.content.ir.ContentIrDocument;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validates normalized IR without making backend-specific assumptions. */
public final class ContentIrValidator {
    public List<ContentDiagnostic> validate(ContentIrDocument document) {
        List<ContentDiagnostic> diagnostics = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        Set<String> identifiers = new HashSet<>();
        for (ContentDefinition definition : document.definitions()) {
            if (!keys.add(definition.stableKey())) {
                diagnostics.add(
                        new ContentDiagnostic(
                                ContentDiagnostic.Severity.ERROR,
                                definition.kind().name().toLowerCase(),
                                "Duplicate Content IR definition",
                                "unique kind + identifier",
                                definition.stableKey(),
                                "Rename or merge the duplicate definition."));
            }
            if (definition.kind() != dev.unifiedmc.content.ir.ContentDefinitionKind.MODEL
                    && definition.kind() != dev.unifiedmc.content.ir.ContentDefinitionKind.TEXTURE
                    && definition.kind()
                            != dev.unifiedmc.content.ir.ContentDefinitionKind.BEHAVIOR) {
                identifiers.add(definition.id().value());
            }
        }
        for (var recipe : document.recipes()) {
            for (var ingredient : recipe.ingredients()) {
                if (ingredient.count() <= 0) {
                    diagnostics.add(
                            new ContentDiagnostic(
                                    ContentDiagnostic.Severity.ERROR,
                                    "recipes." + recipe.id().value(),
                                    "Recipe ingredient count must be positive",
                                    "> 0",
                                    Integer.toString(ingredient.count()),
                                    "Use a positive ingredient count."));
                }
                if (!identifiers.contains(ingredient.item().value())) {
                    diagnostics.add(
                            new ContentDiagnostic(
                                    ContentDiagnostic.Severity.ERROR,
                                    "recipes." + recipe.id().value(),
                                    "Recipe ingredient references unknown content",
                                    "known item or block identifier",
                                    ingredient.item().value(),
                                    "Declare the referenced content before compiling the recipe."));
                }
            }
        }
        return List.copyOf(diagnostics);
    }
}
