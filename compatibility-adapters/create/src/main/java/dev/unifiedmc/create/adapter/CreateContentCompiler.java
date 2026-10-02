package dev.unifiedmc.create.adapter;

import dev.unifiedmc.content.ContentCompilationStatus;
import dev.unifiedmc.create.model.CreateContraptionModel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Deterministic semantic compiler for the Create vertical slice; it emits no Create runtime classes. */
public final class CreateContentCompiler {
    public CompilationResult compile(CreateAdapter adapter) {
        List<String> limitations = new ArrayList<>();
        limitations.addAll(adapter.compatibilityProfile().features().get(dev.unifiedmc.create.profile.CreateFeature.KINETIC_NETWORK).knownLimitations());
        limitations.addAll(adapter.compatibilityProfile().features().get(dev.unifiedmc.create.profile.CreateFeature.CONTRAPTIONS).knownLimitations());
        CreateContraptionModel contraption = CreateContraptionModel.unsupportedPlaceholder("create:contraption");
        String json = "{\n" +
                "  \"adapter\": \"" + adapter.adapterId() + "\",\n" +
                "  \"createVersion\": \"" + dev.unifiedmc.create.profile.CreateCompatibilityProfile.CREATE_VERSION + "\",\n" +
                "  \"minecraft\": \"" + dev.unifiedmc.create.profile.CreateCompatibilityProfile.MINECRAFT_VERSION + "\",\n" +
                "  \"features\": [\"shaft\",\"cogwheel\",\"mechanical_power\",\"kinetic_network\",\"mechanical_press\",\"pressing_recipe\"],\n" +
                "  \"contraption\": \"UNSUPPORTED\"\n" +
                "}\n";
        ContentCompilationStatus status = ContentCompilationStatus.PARTIAL;
        return new CompilationResult(status, json.getBytes(StandardCharsets.UTF_8), List.copyOf(new ArrayList<>(limitations)), contraption);
    }

    public record CompilationResult(ContentCompilationStatus status, byte[] artifact, List<String> limitations, CreateContraptionModel contraption) {
        public CompilationResult { artifact = artifact.clone(); limitations = List.copyOf(limitations); }
        @Override public byte[] artifact() { return artifact.clone(); }
    }
}
