package dev.unifiedmc.content.ir.definitions;

import dev.unifiedmc.content.ir.ContentDefinition;
import dev.unifiedmc.content.ir.ContentDefinitionKind;
import dev.unifiedmc.uapi.content.UniversalIdentifier;
import java.util.Map;

public record ContentTextureDefinition(
        UniversalIdentifier id, String assetPath, Map<String, String> properties)
        implements ContentDefinition {
    public ContentTextureDefinition {
        if (assetPath == null || assetPath.isBlank()) {

            throw new IllegalArgumentException("assetPath must not be blank");
        }
        properties = Map.copyOf(properties);
    }

    @Override
    public ContentDefinitionKind kind() {
        return ContentDefinitionKind.TEXTURE;
    }
}
