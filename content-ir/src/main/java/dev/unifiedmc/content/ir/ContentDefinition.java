package dev.unifiedmc.content.ir;

import dev.unifiedmc.uapi.content.UniversalIdentifier;
import java.util.Map;

/** Common immutable definition contract for Content IR nodes. */
public interface ContentDefinition {
    UniversalIdentifier id();

    ContentDefinitionKind kind();

    Map<String, String> properties();

    default String stableKey() {
        return kind().name() + ":" + id().value();
    }
}
