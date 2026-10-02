package dev.unifiedmc.content.ir.definitions;

import dev.unifiedmc.content.ir.ContentDefinition;
import dev.unifiedmc.content.ir.ContentDefinitionKind;
import dev.unifiedmc.uapi.content.UniversalIdentifier;
import java.util.Map;

public record ContentEntityDefinition(
        UniversalIdentifier id, String entityKind, Map<String, String> properties)
        implements ContentDefinition {
    public ContentEntityDefinition {
        if (entityKind == null || entityKind.isBlank()) {

            throw new IllegalArgumentException("entityKind must not be blank");
        }
        properties = Map.copyOf(properties);
    }

    @Override
    public ContentDefinitionKind kind() {
        return ContentDefinitionKind.ENTITY;
    }
}
