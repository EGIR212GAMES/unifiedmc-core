package dev.unifiedmc.examples.uapi;

import dev.unifiedmc.mod.ModLoader;
import dev.unifiedmc.uapi.Capability;
import dev.unifiedmc.uapi.adapter.ModAdapter;
import dev.unifiedmc.uapi.compat.CompatibilityContext;
import dev.unifiedmc.uapi.compat.CompatibilityProfile;
import dev.unifiedmc.uapi.content.UniversalBlock;
import dev.unifiedmc.uapi.content.UniversalContentBundle;
import dev.unifiedmc.uapi.content.UniversalEntity;
import dev.unifiedmc.uapi.content.UniversalIdentifier;
import dev.unifiedmc.uapi.content.UniversalItem;
import dev.unifiedmc.uapi.content.UniversalRecipe;
import dev.unifiedmc.uapi.representation.ContentRepresentations;
import dev.unifiedmc.uapi.representation.UniversalRepresentationBundle;
import dev.unifiedmc.uapi.representation.bedrock.BedrockRepresentation;
import dev.unifiedmc.uapi.representation.java.JavaRepresentation;
import dev.unifiedmc.uapi.representation.server.ServerSideRepresentation;
import dev.unifiedmc.version.GameVersion;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Documentation/example adapter only. It demonstrates UAPI contracts and contains no Minecraft
 * implementation classes.
 */
public final class ExampleModAdapter implements ModAdapter {
    private static final UniversalIdentifier BLOCK_ID =
            new UniversalIdentifier("example", "copper_block");
    private static final UniversalIdentifier ITEM_ID =
            new UniversalIdentifier("example", "copper_wrench");
    private static final UniversalIdentifier ENTITY_ID =
            new UniversalIdentifier("example", "clockwork_golem");
    private static final UniversalIdentifier RECIPE_ID =
            new UniversalIdentifier("example", "copper_wrench_recipe");

    private static final CompatibilityProfile PROFILE =
            CompatibilityProfile.builder()
                    .minecraftVersion("26.3")
                    .loader(ModLoader.NEOFORGE)
                    .serverSide(true)
                    .polymerRepresentation(true)
                    .bedrockRepresentation(true)
                    .capabilities(
                            Capability.BLOCKS,
                            Capability.ITEMS,
                            Capability.ENTITIES,
                            Capability.RECIPES,
                            Capability.SERVER_SIDE,
                            Capability.POLYMER_REPRESENTATION,
                            Capability.BEDROCK)
                    .limitation(
                            "Example entity rendering is illustrative; no client renderer is provided")
                    .build();

    @Override
    public String adapterId() {
        return "example-mod-adapter";
    }

    @Override
    public String modId() {
        return "example";
    }

    @Override
    public CompatibilityProfile profile() {
        return PROFILE;
    }

    @Override
    public UniversalContentBundle content(CompatibilityContext context) {
        return new UniversalContentBundle(
                List.of(new ExampleBlock()),
                List.of(new ExampleItem()),
                List.of(new ExampleEntity()),
                List.of(),
                List.of(new ExampleRecipe()),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    @Override
    public UniversalRepresentationBundle representations(CompatibilityContext context) {
        return new UniversalRepresentationBundle(
                Map.of(
                        BLOCK_ID,
                                new ContentRepresentations(
                                        javaRepresentation(BLOCK_ID, "block"),
                                        polymerRepresentation(BLOCK_ID, "minecraft:note_block"),
                                        bedrockRepresentation(BLOCK_ID, "example:copper_block")),
                        ITEM_ID,
                                new ContentRepresentations(
                                        javaRepresentation(ITEM_ID, "item"),
                                        polymerRepresentation(ITEM_ID, "minecraft:stick"),
                                        bedrockRepresentation(ITEM_ID, "example:copper_wrench")),
                        ENTITY_ID,
                                new ContentRepresentations(
                                        javaRepresentation(ENTITY_ID, "entity"),
                                        polymerRepresentation(ENTITY_ID, "minecraft:armor_stand"),
                                        bedrockRepresentation(
                                                ENTITY_ID, "example:clockwork_golem")),
                        RECIPE_ID,
                                new ContentRepresentations(
                                        javaRepresentation(RECIPE_ID, "recipe"),
                                        java.util.Optional.empty(),
                                        bedrockRepresentation(
                                                RECIPE_ID, "example:crafting_recipe"))));
    }

    private static java.util.Optional<JavaRepresentation> javaRepresentation(
            UniversalIdentifier id, String type) {
        return java.util.Optional.of(
                new JavaRepresentation(id, type, id.value(), Map.of("mode", "example")));
    }

    private static java.util.Optional<ServerSideRepresentation> polymerRepresentation(
            UniversalIdentifier id, String vanillaTarget) {
        return java.util.Optional.of(
                new ServerSideRepresentation(
                        id,
                        ServerSideRepresentation.ProjectionKind.POLYMER,
                        vanillaTarget,
                        Map.of("mode", "illustrative")));
    }

    private static java.util.Optional<BedrockRepresentation> bedrockRepresentation(
            UniversalIdentifier id, String identifier) {
        return java.util.Optional.of(
                new BedrockRepresentation(
                        id, identifier, "example/resources", "example/behavior", Map.of()));
    }

    private record ExampleBlock(UniversalIdentifier id) implements UniversalBlock {
        private ExampleBlock() {
            this(BLOCK_ID);
        }

        @Override
        public Set<Capability> capabilities() {
            return Set.of(
                    Capability.BLOCKS,
                    Capability.SERVER_SIDE,
                    Capability.POLYMER_REPRESENTATION,
                    Capability.BEDROCK);
        }

        @Override
        public Map<String, String> attributes() {
            return Map.of(
                    "material",
                    "copper",
                    "model",
                    "example:copper_block",
                    "texture",
                    "example:copper_block");
        }

        @Override
        public float hardness() {
            return 3.0f;
        }
    }

    private record ExampleItem(UniversalIdentifier id) implements UniversalItem {
        private ExampleItem() {
            this(ITEM_ID);
        }

        @Override
        public Set<Capability> capabilities() {
            return Set.of(
                    Capability.ITEMS,
                    Capability.SERVER_SIDE,
                    Capability.POLYMER_REPRESENTATION,
                    Capability.BEDROCK);
        }

        @Override
        public Map<String, String> attributes() {
            return Map.of(
                    "tool",
                    "wrench",
                    "model",
                    "example:copper_wrench",
                    "texture",
                    "example:copper_wrench");
        }

        @Override
        public int maxStackSize() {
            return 1;
        }
    }

    private record ExampleEntity(UniversalIdentifier id) implements UniversalEntity {
        private ExampleEntity() {
            this(ENTITY_ID);
        }

        @Override
        public Set<Capability> capabilities() {
            return Set.of(Capability.ENTITIES, Capability.SERVER_SIDE, Capability.BEDROCK);
        }

        @Override
        public Map<String, String> attributes() {
            return Map.of(
                    "health",
                    "20",
                    "size",
                    "1.0",
                    "model",
                    "example:clockwork_golem",
                    "behavior",
                    "construct");
        }

        @Override
        public String entityKind() {
            return "construct";
        }
    }

    private record ExampleRecipe(UniversalIdentifier id) implements UniversalRecipe {
        private ExampleRecipe() {
            this(RECIPE_ID);
        }

        @Override
        public Set<Capability> capabilities() {
            return Set.of(Capability.RECIPES, Capability.SERVER_SIDE);
        }

        @Override
        public Map<String, String> attributes() {
            return Map.of("category", "example");
        }

        @Override
        public String recipeType() {
            return "crafting_shaped";
        }

        @Override
        public List<Ingredient> ingredients() {
            return List.of(new Ingredient(BLOCK_ID, 1));
        }

        @Override
        public Ingredient result() {
            return new Ingredient(ITEM_ID, 1);
        }
    }

    public static CompatibilityContext targetContext() {
        return new CompatibilityContext(
                new GameVersion("26.3"),
                ModLoader.NEOFORGE,
                true,
                true,
                true,
                true,
                Set.of(
                        Capability.BLOCKS,
                        Capability.ITEMS,
                        Capability.ENTITIES,
                        Capability.RECIPES));
    }
}
