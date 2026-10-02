package dev.unifiedmc.core;

import dev.unifiedmc.config.CoreConfiguration;
import dev.unifiedmc.runtime.BackendDescriptor;

/** Boundary for pack/resource preparation; unfinished providers must report unsupported. */
@FunctionalInterface
public interface ResourcePreparation {
    ResourcePreparationResult prepare(CoreConfiguration configuration, BackendDescriptor backend);
}
