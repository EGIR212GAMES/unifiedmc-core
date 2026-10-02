package dev.unifiedmc.content.ir.definitions;

import dev.unifiedmc.content.ir.ContentDefinition;
import dev.unifiedmc.content.ir.ContentDefinitionKind;
import dev.unifiedmc.uapi.content.UniversalIdentifier;
import java.util.Map;

public record ContentBlockDefinition(
        UniversalIdentifier id, float hardness, Map<String, String> properties)
        implements ContentDefinition {
    public ContentBlockDefinition {
        properties = Map.copyOf(properties);
    }

    @Override
    public ContentDefinitionKind kind() {
        return ContentDefinitionKind.BLOCK;
    }
}
