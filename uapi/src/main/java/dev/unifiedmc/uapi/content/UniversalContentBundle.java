package dev.unifiedmc.uapi.content;

import java.util.List;

/** Collection of normalized content declared by an adapter for one compatibility target. */
public record UniversalContentBundle(
        List<UniversalBlock> blocks,
        List<UniversalItem> items,
        List<UniversalEntity> entities,
        List<UniversalFluid> fluids,
        List<UniversalRecipe> recipes,
        List<UniversalMachine> machines,
        List<UniversalContraption> contraptions,
        List<UniversalDataComponent> dataComponents,
        List<UniversalPacket> packets,
        List<UniversalSound> sounds,
        List<UniversalParticle> particles,
        List<UniversalWorldInteraction> worldInteractions) {
    public UniversalContentBundle {
        blocks = List.copyOf(blocks);
        items = List.copyOf(items);
        entities = List.copyOf(entities);
        fluids = List.copyOf(fluids);
        recipes = List.copyOf(recipes);
        machines = List.copyOf(machines);
        contraptions = List.copyOf(contraptions);
        dataComponents = List.copyOf(dataComponents);
        packets = List.copyOf(packets);
        sounds = List.copyOf(sounds);
        particles = List.copyOf(particles);
        worldInteractions = List.copyOf(worldInteractions);
    }

    public static UniversalContentBundle empty() {
        return new UniversalContentBundle(
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
