package dev.unifiedmc.create.model;

import java.util.List;

/** Explicit placeholder for Create moving contraptions; dynamic assembly is not in the first slice. */
public record CreateContraptionModel(String id, List<CreateKineticModel> components, boolean dynamic) {
    public CreateContraptionModel {
        components = List.copyOf(components);
    }

    public static CreateContraptionModel unsupportedPlaceholder(String id) {
        return new CreateContraptionModel(id, List.of(), true);
    }
}
