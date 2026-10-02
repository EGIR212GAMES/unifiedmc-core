package dev.unifiedmc.create.adapter;

import dev.unifiedmc.create.profile.CreateCompatibilityProfile;
import dev.unifiedmc.create.profile.CreateSupportTier;
import dev.unifiedmc.mod.ModLoader;
import dev.unifiedmc.uapi.Capability;
import dev.unifiedmc.uapi.adapter.ModAdapter;
import dev.unifiedmc.uapi.compat.CompatibilityContext;
import dev.unifiedmc.uapi.compat.CompatibilityProfile;
import dev.unifiedmc.uapi.compat.CompatibilityReport;
import dev.unifiedmc.uapi.content.UniversalBlock;
import dev.unifiedmc.uapi.content.UniversalContentBundle;
import dev.unifiedmc.uapi.content.UniversalIdentifier;
import dev.unifiedmc.uapi.content.UniversalMachine;
import dev.unifiedmc.uapi.content.UniversalRecipe;
import dev.unifiedmc.uapi.representation.ContentRepresentations;
import dev.unifiedmc.uapi.representation.UniversalRepresentationBundle;
import dev.unifiedmc.uapi.representation.bedrock.BedrockRepresentation;
import dev.unifiedmc.uapi.representation.java.JavaRepresentation;
import dev.unifiedmc.uapi.representation.server.ServerSideRepresentation;
import dev.unifiedmc.version.GameVersion;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Minimal Create adapter. It consumes Create semantics without loading Create classes into UnifiedMC Core.
 */
public final class CreateAdapter implements ModAdapter {
    public static final UniversalIdentifier SHAFT_ID = new UniversalIdentifier("create", "shaft");
    public static final UniversalIdentifier COGWHEEL_ID = new UniversalIdentifier("create", "cogwheel");
    public static final UniversalIdentifier PRESS_ID = new UniversalIdentifier("create", "mechanical_press");
    public static final UniversalIdentifier PRESS_RECIPE_ID = new UniversalIdentifier("create", "pressing/example_ingot");

    private static final CreateCompatibilityProfile CREATE_PROFILE = new CreateCompatibilityProfile();

    @Override public String adapterId() { return "create-6.0.10-1.21.1"; }
    @Override public String modId() { return "create"; }
    @Override public CompatibilityProfile profile() { return CREATE_PROFILE.uapiProfile(); }

    @Override
    public CompatibilityReport compatibility(CompatibilityContext context) {
        CompatibilityReport base = ModAdapter.super.compatibility(context);
        if (base.status() == CompatibilityReport.Status.UNSUPPORTED) {
            return base;
        }
        if (context.bedrockRequested()) {
            List<String> limitations = new java.util.ArrayList<>(base.limitations());
            List<String> unsupported = new java.util.ArrayList<>(base.unsupportedFeatures());
            boolean partial = false;
            for (var support : CREATE_PROFILE.features().values()) {
                if (support.bedrockVisualSupport() && !support.bedrockInteractionSupport()) {
                    partial = true;
                    limitations.addAll(support.knownLimitations());
                }
            }
            if (partial && base.status() == CompatibilityReport.Status.SUPPORTED) {
                return new CompatibilityReport(
                        CompatibilityReport.Status.PARTIAL, base.adapterId(), base.supportedCapabilities(),
                        base.unsupportedCapabilities(), List.copyOf(unsupported), List.copyOf(limitations));
            }
        }
        return base;
    }

    @Override
    public UniversalContentBundle content(CompatibilityContext context) {
        return new UniversalContentBundle(
                List.of(new Shaft(), new Cogwheel(), new MechanicalPress()),
                List.of(), List.of(), List.of(), List.of(new PressingRecipe()),
                List.of(new PressMachine()), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    @Override
    public UniversalRepresentationBundle representations(CompatibilityContext context) {
        return new UniversalRepresentationBundle(Map.of(
                SHAFT_ID, reps(SHAFT_ID, "block", "minecraft:note_block", "create:shaft"),
                COGWHEEL_ID, reps(COGWHEEL_ID, "block", "minecraft:note_block", "create:cogwheel"),
                PRESS_ID, reps(PRESS_ID, "block", "minecraft:dropper", "create:mechanical_press"),
                PRESS_RECIPE_ID, new ContentRepresentations(
                        Optional.of(new JavaRepresentation(PRESS_RECIPE_ID, "recipe", PRESS_RECIPE_ID.value(), Map.of("type", "create:pressing"))),
                        Optional.empty(),
                        Optional.of(new BedrockRepresentation(PRESS_RECIPE_ID, "create:pressing/example", "create", "create", Map.of("status", "partial"))))));
    }

    public CreateCompatibilityProfile compatibilityProfile() { return CREATE_PROFILE; }

    private static ContentRepresentations reps(UniversalIdentifier id, String javaKind, String polymerTarget, String bedrockId) {
        return new ContentRepresentations(
                Optional.of(new JavaRepresentation(id, javaKind, id.value(), Map.of("createVersion", CreateCompatibilityProfile.CREATE_VERSION))),
                Optional.of(new ServerSideRepresentation(id, ServerSideRepresentation.ProjectionKind.POLYMER, polymerTarget, Map.of("createVersion", CreateCompatibilityProfile.CREATE_VERSION))),
                Optional.of(new BedrockRepresentation(id, bedrockId, "create", "create", Map.of("compatibility", "explicit"))));
    }

    private record Shaft() implements UniversalBlock {
        @Override public UniversalIdentifier id() { return SHAFT_ID; }
        @Override public Set<Capability> capabilities() { return Set.of(Capability.BLOCKS, Capability.SERVER_SIDE, Capability.POLYMER_REPRESENTATION, Capability.BEDROCK); }
        @Override public Map<String, String> attributes() { return Map.of("axis", "x|y|z", "kinetic", "true", "createSupport", CreateSupportTier.SERVER_SIDE_EMULATABLE.name()); }
        @Override public float hardness() { return 2.0f; }
    }

    private record Cogwheel() implements UniversalBlock {
        @Override public UniversalIdentifier id() { return COGWHEEL_ID; }
        @Override public Set<Capability> capabilities() { return Set.of(Capability.BLOCKS, Capability.SERVER_SIDE, Capability.POLYMER_REPRESENTATION, Capability.BEDROCK); }
        @Override public Map<String, String> attributes() { return Map.of("axis", "x|y|z", "kinetic", "true", "gearSize", "small"); }
        @Override public float hardness() { return 2.0f; }
    }

    private record MechanicalPress() implements UniversalBlock {
        @Override public UniversalIdentifier id() { return PRESS_ID; }
        @Override public Set<Capability> capabilities() { return Set.of(Capability.BLOCKS, Capability.MACHINES, Capability.SERVER_SIDE, Capability.POLYMER_REPRESENTATION, Capability.BEDROCK); }
        @Override public Map<String, String> attributes() { return Map.of("machine", "mechanical_press", "kinetic", "true", "processing", "pressing"); }
        @Override public float hardness() { return 3.0f; }
    }

    private record PressMachine() implements UniversalMachine {
        @Override public UniversalIdentifier id() { return PRESS_ID; }
        @Override public Set<Capability> capabilities() { return Set.of(Capability.MACHINES, Capability.SERVER_SIDE, Capability.RECIPES); }
        @Override public Map<String, String> attributes() { return Map.of("processing", "pressing", "inputSlots", "1", "kineticRequired", "true"); }
        @Override public String machineKind() { return "mechanical_press"; }
    }

    private record PressingRecipe() implements UniversalRecipe {
        private static final UniversalIdentifier EXAMPLE_INGOT = new UniversalIdentifier("minecraft", "iron_ingot");
        private static final UniversalIdentifier EXAMPLE_OUTPUT = new UniversalIdentifier("minecraft", "iron_nugget");
        @Override public UniversalIdentifier id() { return PRESS_RECIPE_ID; }
        @Override public Set<Capability> capabilities() { return Set.of(Capability.RECIPES, Capability.SERVER_SIDE, Capability.MACHINES); }
        @Override public Map<String, String> attributes() { return Map.of("machine", "create:mechanical_press", "type", "create:pressing", "processingTime", "240"); }
        @Override public String recipeType() { return "create:pressing"; }
        @Override public List<Ingredient> ingredients() { return List.of(new Ingredient(EXAMPLE_INGOT, 1)); }
        @Override public Ingredient result() { return new Ingredient(EXAMPLE_OUTPUT, 1); }
    }

    public static CompatibilityContext targetContext() {
        return new CompatibilityContext(new GameVersion(CreateCompatibilityProfile.MINECRAFT_VERSION), ModLoader.NEOFORGE,
                true, true, true, true, Set.of(Capability.BLOCKS, Capability.MACHINES, Capability.RECIPES, Capability.SERVER_SIDE));
    }
}
