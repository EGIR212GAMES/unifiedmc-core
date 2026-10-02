package dev.unifiedmc.geyser.bridge;

import dev.unifiedmc.geyser.BedrockCompilationResult;
import java.nio.file.Path;

/**
 * Deployment boundary for Geyser. The current implementation is file-based and API-version
 * agnostic.
 */
public interface GeyserBridge {
    GeyserBridgeResult deploy(BedrockCompilationResult result, GeyserDeploymentTarget target);

    record GeyserDeploymentTarget(
            Path customMappingsDirectory,
            Path packsDirectory,
            boolean requireCustomContentEnabled) {}
}
