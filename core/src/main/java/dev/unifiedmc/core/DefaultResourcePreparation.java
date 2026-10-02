package dev.unifiedmc.core;

import dev.unifiedmc.config.CoreConfiguration;
import dev.unifiedmc.runtime.BackendDescriptor;
import java.util.List;

/** Safe baseline resource stage; it never pretends to compile packs that are not implemented. */
public final class DefaultResourcePreparation implements ResourcePreparation {
    @Override
    public ResourcePreparationResult prepare(
            CoreConfiguration configuration, BackendDescriptor backend) {
        if (configuration.bedrock().enabled() && configuration.bedrock().resourcePacks()) {
            return new ResourcePreparationResult(
                    ResourcePreparationResult.Status.UNSUPPORTED,
                    List.of(
                            "Bedrock resource-pack compilation is not implemented in the orchestration baseline"));
        }
        return new ResourcePreparationResult(
                ResourcePreparationResult.Status.NOT_REQUIRED, List.of());
    }
}
