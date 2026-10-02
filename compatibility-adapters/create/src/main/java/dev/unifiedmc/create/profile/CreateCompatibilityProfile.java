package dev.unifiedmc.create.profile;

import dev.unifiedmc.mod.ModLoader;
import dev.unifiedmc.uapi.Capability;
import dev.unifiedmc.uapi.compat.CompatibilityProfile;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Compatibility contract for the first Create adapter slice.
 *
 * <p>Stable target: Create 6.0.10 on Minecraft 1.21.1 / NeoForge. The Create project's
 * 1.21.1 development branch currently carries unreleased 6.0.11 metadata, so this adapter does
 * not advertise that development version as a released target.
 */
public final class CreateCompatibilityProfile {
    public static final String CREATE_VERSION = "6.0.10";
    public static final String MINECRAFT_VERSION = "1.21.1";

    private final CompatibilityProfile uapiProfile;
    private final Map<CreateFeature, CreateFeatureSupport> features;
    private final List<String> dependencies;

    public CreateCompatibilityProfile() {
        this.features = buildFeatures();
        this.dependencies = List.of(
                "flywheel",
                "ponder",
                "neoforge 21.1.x",
                "minecraft 1.21.1");
        this.uapiProfile = CompatibilityProfile.builder()
                .minecraftVersion(MINECRAFT_VERSION)
                .loader(ModLoader.NEOFORGE)
                .serverSide(true)
                .polymerRepresentation(true)
                .bedrockRepresentation(true)
                .capabilities(
                        Capability.BLOCKS,
                        Capability.ITEMS,
                        Capability.RECIPES,
                        Capability.MACHINES,
                        Capability.SERVER_SIDE,
                        Capability.POLYMER_REPRESENTATION,
                        Capability.BEDROCK)
                .limitation("This adapter targets a documented vertical slice, not the full Create feature set")
                .limitation("Create client rendering/Flywheel visuals are not executed by UnifiedMC Core")
                .limitation("Contraption simulation is not part of the first vertical slice")
                .limitation("Bedrock visual and interaction support is assessed explicitly per feature")
                .build();
    }

    public CompatibilityProfile uapiProfile() {
        return uapiProfile;
    }

    public Map<CreateFeature, CreateFeatureSupport> features() {
        return features;
    }

    public List<String> dependencies() {
        return dependencies;
    }

    private static Map<CreateFeature, CreateFeatureSupport> buildFeatures() {
        Map<CreateFeature, CreateFeatureSupport> result = new LinkedHashMap<>();
        add(result, CreateFeature.SHAFT, CreateSupportTier.SERVER_SIDE_EMULATABLE, true, true, true, true, true,
                "low", List.of("UnifiedMC does not use Create client renderer for shaft visuals"));
        add(result, CreateFeature.COGWHEEL, CreateSupportTier.SERVER_SIDE_EMULATABLE, true, true, true, true, true,
                "medium", List.of("This slice models simple kinetic linkage; the complete Create gear-meshing matrix is not reproduced"));
        add(result, CreateFeature.MECHANICAL_POWER, CreateSupportTier.SERVER_SIDE_EMULATABLE, true, true, true, true, true,
                "medium", List.of("External power source is represented as a normalized server-side source, not Create's complete generator catalogue"));
        add(result, CreateFeature.KINETIC_NETWORK, CreateSupportTier.SERVER_NATIVE, true, true, true, true, true,
                "medium", List.of("Stress/capacity are normalized; complete Create stress configuration is not reproduced"));
        add(result, CreateFeature.MECHANICAL_PRESS, CreateSupportTier.SERVER_SIDE_EMULATABLE, true, true, true, true, true,
                "medium", List.of("Only the single-machine world-input pressing path is modeled in this slice"));
        add(result, CreateFeature.PRESSING_RECIPE, CreateSupportTier.SERVER_NATIVE, true, false, true, true, true,
                "low", List.of("Only one deterministic demonstration recipe is exposed by the adapter"));
        add(result, CreateFeature.CONTRAPTIONS, CreateSupportTier.UNSUPPORTED, false, false, false, false, true,
                "very high", List.of("Create contraptions include moving structure capture, collision worlds, actors and client synchronization; not implemented in this slice"));
        add(result, CreateFeature.KINETIC_RENDERING, CreateSupportTier.POLYMER_REPRESENTABLE, false, false, true, false, false,
                "high", List.of("Dynamic Flywheel visuals are not portable to a generic server-side representation"));
        add(result, CreateFeature.FLYWHEEL_VISUALS, CreateSupportTier.CLIENT_ONLY, false, true, false, false, false,
                "very high", List.of("Flywheel is a client rendering/runtime dependency in Create's 1.21.1 build"));
        add(result, CreateFeature.CREATE_NETWORK_PACKETS, CreateSupportTier.UNSUPPORTED, false, false, false, false, false,
                "high", List.of("Create-specific packet schemas are not reimplemented; semantic state is exchanged through UnifiedMC backend APIs"));
        add(result, CreateFeature.BEDROCK_CUSTOM_BLOCKS, CreateSupportTier.BEDROCK_REPRESENTABLE, true, true, true, true, true,
                "medium", List.of("Requires explicit Bedrock mappings and assets; no automatic Java pack conversion"));
        add(result, CreateFeature.BEDROCK_INTERACTION, CreateSupportTier.POLYMER_REPRESENTABLE, false, false, true, false, true,
                "high", List.of("Bedrock interaction semantics are explicit and partial; arbitrary Create block behavior is not inferred"));
        add(result, CreateFeature.JAVA_CLIENT_RENDERING, CreateSupportTier.CLIENT_ONLY, false, true, false, false, false,
                "high", List.of("A Java client without Create does not receive the full Create renderer"));
        return Map.copyOf(result);
    }

    private static void add(Map<CreateFeature, CreateFeatureSupport> target, CreateFeature feature,
                            CreateSupportTier tier, boolean functional, boolean javaVisual,
                            boolean bedrockVisual, boolean bedrockInteraction, boolean persistence,
                            String networking, List<String> limitations) {
        target.put(feature, new CreateFeatureSupport(feature, tier, functional, javaVisual,
                bedrockVisual, bedrockInteraction, persistence, networking, limitations));
    }
}
