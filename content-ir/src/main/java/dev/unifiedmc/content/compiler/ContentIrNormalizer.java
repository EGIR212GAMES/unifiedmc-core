package dev.unifiedmc.content.compiler;

import dev.unifiedmc.content.ir.ContentIrDocument;
import dev.unifiedmc.uapi.compat.CompatibilityContext;
import dev.unifiedmc.uapi.content.UniversalContent;
import dev.unifiedmc.uapi.content.UniversalContentBundle;
import dev.unifiedmc.uapi.content.UniversalIdentifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Converts UAPI semantics to backend-neutral Content IR without importing backend APIs. */
public final class ContentIrNormalizer {
    public ContentIrDocument normalize(
            String adapterId,
            String modId,
            UniversalContentBundle bundle,
            CompatibilityContext context) {
        List<dev.unifiedmc.content.ir.definitions.ContentBlockDefinition> blocks =
                new ArrayList<>();
        bundle.blocks()
                .forEach(
                        block ->
                                blocks.add(
                                        new dev.unifiedmc.content.ir.definitions
                                                .ContentBlockDefinition(
                                                block.id(), block.hardness(), block.attributes())));
        List<dev.unifiedmc.content.ir.definitions.ContentItemDefinition> items = new ArrayList<>();
        bundle.items()
                .forEach(
                        item ->
                                items.add(
                                        new dev.unifiedmc.content.ir.definitions
                                                .ContentItemDefinition(
                                                item.id(),
                                                item.maxStackSize(),
                                                item.attributes())));
        List<dev.unifiedmc.content.ir.definitions.ContentEntityDefinition> entities =
                new ArrayList<>();
        bundle.entities()
                .forEach(
                        entity ->
                                entities.add(
                                        new dev.unifiedmc.content.ir.definitions
                                                .ContentEntityDefinition(
                                                entity.id(),
                                                entity.entityKind(),
                                                entity.attributes())));
        List<dev.unifiedmc.content.ir.definitions.ContentRecipeDefinition> recipes =
                new ArrayList<>();
        bundle.recipes()
                .forEach(
                        recipe ->
                                recipes.add(
                                        new dev.unifiedmc.content.ir.definitions
                                                .ContentRecipeDefinition(
                                                recipe.id(),
                                                recipe.recipeType(),
                                                recipe.ingredients(),
                                                recipe.result(),
                                                recipe.attributes())));

        List<dev.unifiedmc.content.ir.definitions.ContentModelDefinition> models =
                new ArrayList<>();
        List<dev.unifiedmc.content.ir.definitions.ContentTextureDefinition> textures =
                new ArrayList<>();
        List<dev.unifiedmc.content.ir.definitions.ContentBehaviorDefinition> behaviors =
                new ArrayList<>();
        bundle.blocks()
                .forEach(block -> deriveHints(block, block.id(), models, textures, behaviors));
        bundle.items().forEach(item -> deriveHints(item, item.id(), models, textures, behaviors));
        bundle.entities()
                .forEach(entity -> deriveHints(entity, entity.id(), models, textures, behaviors));

        Set<dev.unifiedmc.uapi.Capability> capabilities = new LinkedHashSet<>();
        bundle.blocks().forEach(v -> capabilities.addAll(v.capabilities()));
        bundle.items().forEach(v -> capabilities.addAll(v.capabilities()));
        bundle.entities().forEach(v -> capabilities.addAll(v.capabilities()));
        bundle.fluids().forEach(v -> capabilities.addAll(v.capabilities()));
        bundle.recipes().forEach(v -> capabilities.addAll(v.capabilities()));
        bundle.machines().forEach(v -> capabilities.addAll(v.capabilities()));
        bundle.contraptions().forEach(v -> capabilities.addAll(v.capabilities()));
        bundle.dataComponents().forEach(v -> capabilities.addAll(v.capabilities()));
        bundle.packets().forEach(v -> capabilities.addAll(v.capabilities()));
        bundle.sounds().forEach(v -> capabilities.addAll(v.capabilities()));
        bundle.particles().forEach(v -> capabilities.addAll(v.capabilities()));
        bundle.worldInteractions().forEach(v -> capabilities.addAll(v.capabilities()));

        return new ContentIrDocument(
                dev.unifiedmc.content.ir.ContentIrVersion.V1,
                adapterId,
                modId,
                context.minecraftVersion(),
                context.loader(),
                capabilities,
                blocks,
                items,
                entities,
                recipes,
                models,
                textures,
                behaviors);
    }

    public List<String> unsupportedSourceFeatures(UniversalContentBundle bundle) {
        List<String> unsupported = new ArrayList<>();
        if (!bundle.fluids().isEmpty()) {

            unsupported.add("FLUIDS");
        }
        if (!bundle.machines().isEmpty()) {

            unsupported.add("MACHINES");
        }
        if (!bundle.contraptions().isEmpty()) {

            unsupported.add("CONTRAPTIONS");
        }
        if (!bundle.dataComponents().isEmpty()) {

            unsupported.add("DATA_COMPONENTS");
        }
        if (!bundle.packets().isEmpty()) {

            unsupported.add("NETWORKING");
        }
        if (!bundle.sounds().isEmpty()) {

            unsupported.add("SOUNDS");
        }
        if (!bundle.particles().isEmpty()) {

            unsupported.add("PARTICLES");
        }
        if (!bundle.worldInteractions().isEmpty()) {

            unsupported.add("WORLD_INTERACTION");
        }
        return List.copyOf(unsupported);
    }

    private void deriveHints(
            UniversalContent content,
            UniversalIdentifier id,
            List<dev.unifiedmc.content.ir.definitions.ContentModelDefinition> models,
            List<dev.unifiedmc.content.ir.definitions.ContentTextureDefinition> textures,
            List<dev.unifiedmc.content.ir.definitions.ContentBehaviorDefinition> behaviors) {
        String model = content.attributes().get("model");
        if (model != null && !model.isBlank()) {
            models.add(
                    new dev.unifiedmc.content.ir.definitions.ContentModelDefinition(
                            id, "universal", model, java.util.Map.of("source", "uapi-attribute")));
        }
        String texture = content.attributes().get("texture");
        if (texture != null && !texture.isBlank()) {
            textures.add(
                    new dev.unifiedmc.content.ir.definitions.ContentTextureDefinition(
                            id, texture, java.util.Map.of("source", "uapi-attribute")));
        }
        String behavior = content.attributes().get("behavior");
        if (behavior != null && !behavior.isBlank()) {
            behaviors.add(
                    new dev.unifiedmc.content.ir.definitions.ContentBehaviorDefinition(
                            id, behavior, java.util.Map.of("source", "uapi-attribute")));
        }
    }
}
