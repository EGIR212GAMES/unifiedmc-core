package dev.unifiedmc.content.ir.definitions;

import dev.unifiedmc.content.ir.ContentDefinition;
import dev.unifiedmc.content.ir.ContentDefinitionKind;
import dev.unifiedmc.uapi.content.UniversalIdentifier;
import java.util.Map;

public record ContentItemDefinition(
        UniversalIdentifier id, int maxStackSize, Map<String, String> properties)
        implements ContentDefinition {
    public ContentItemDefinition {
        if (maxStackSize <= 0) {

            throw new IllegalArgumentException("maxStackSize must be > 0");
        }
        properties = Map.copyOf(properties);
    }

    @Override
    public ContentDefinitionKind kind() {
        return ContentDefinitionKind.ITEM;
    }
}
