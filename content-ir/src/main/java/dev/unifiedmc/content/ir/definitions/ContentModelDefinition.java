package dev.unifiedmc.content.ir.definitions;

import dev.unifiedmc.content.ir.ContentDefinition;
import dev.unifiedmc.content.ir.ContentDefinitionKind;
import dev.unifiedmc.uapi.content.UniversalIdentifier;
import java.util.Map;

public record ContentModelDefinition(
        UniversalIdentifier id, String modelType, String source, Map<String, String> properties)
        implements ContentDefinition {
    public ContentModelDefinition {
        if (modelType == null || modelType.isBlank()) {

            throw new IllegalArgumentException("modelType must not be blank");
        }
        if (source == null || source.isBlank()) {

            throw new IllegalArgumentException("source must not be blank");
        }
        properties = Map.copyOf(properties);
    }

    @Override
    public ContentDefinitionKind kind() {
        return ContentDefinitionKind.MODEL;
    }
}
