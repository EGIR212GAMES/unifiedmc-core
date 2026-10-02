package dev.unifiedmc.content.ir.definitions;

import dev.unifiedmc.content.ir.ContentDefinition;
import dev.unifiedmc.content.ir.ContentDefinitionKind;
import dev.unifiedmc.uapi.content.UniversalIdentifier;
import java.util.Map;

public record ContentBehaviorDefinition(
        UniversalIdentifier id, String behaviorType, Map<String, String> properties)
        implements ContentDefinition {
    public ContentBehaviorDefinition {
        if (behaviorType == null || behaviorType.isBlank()) {

            throw new IllegalArgumentException("behaviorType must not be blank");
        }
        properties = Map.copyOf(properties);
    }

    @Override
    public ContentDefinitionKind kind() {
        return ContentDefinitionKind.BEHAVIOR;
    }
}
