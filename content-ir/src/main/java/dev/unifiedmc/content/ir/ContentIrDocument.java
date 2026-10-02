package dev.unifiedmc.content.ir;

import dev.unifiedmc.content.ir.definitions.ContentBehaviorDefinition;
import dev.unifiedmc.content.ir.definitions.ContentBlockDefinition;
import dev.unifiedmc.content.ir.definitions.ContentEntityDefinition;
import dev.unifiedmc.content.ir.definitions.ContentItemDefinition;
import dev.unifiedmc.content.ir.definitions.ContentModelDefinition;
import dev.unifiedmc.content.ir.definitions.ContentRecipeDefinition;
import dev.unifiedmc.content.ir.definitions.ContentTextureDefinition;
import dev.unifiedmc.mod.ModLoader;
import dev.unifiedmc.uapi.Capability;
import dev.unifiedmc.version.GameVersion;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Complete normalized Content IR document, sorted for deterministic compilation. */
public record ContentIrDocument(
        ContentIrVersion irVersion,
        String adapterId,
        String modId,
        GameVersion minecraftVersion,
        ModLoader loader,
        Set<Capability> capabilities,
        List<ContentBlockDefinition> blocks,
        List<ContentItemDefinition> items,
        List<ContentEntityDefinition> entities,
        List<ContentRecipeDefinition> recipes,
        List<ContentModelDefinition> models,
        List<ContentTextureDefinition> textures,
        List<ContentBehaviorDefinition> behaviors) {
    public ContentIrDocument {
        capabilities = Set.copyOf(capabilities);
        blocks = sorted(blocks);
        items = sorted(items);
        entities = sorted(entities);
        recipes = sorted(recipes);
        models = sorted(models);
        textures = sorted(textures);
        behaviors = sorted(behaviors);
    }

    private static <T extends ContentDefinition> List<T> sorted(List<T> values) {
        return values.stream().sorted(Comparator.comparing(ContentDefinition::stableKey)).toList();
    }

    public List<ContentDefinition> definitions() {
        return java.util.stream.Stream.of(
                        blocks.stream(),
                        items.stream(),
                        entities.stream(),
                        recipes.stream(),
                        models.stream(),
                        textures.stream(),
                        behaviors.stream())
                .flatMap(s -> s.map(v -> (ContentDefinition) v))
                .sorted(Comparator.comparing(ContentDefinition::stableKey))
                .toList();
    }
}
