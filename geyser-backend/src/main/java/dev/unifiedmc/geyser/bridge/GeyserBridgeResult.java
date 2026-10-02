package dev.unifiedmc.geyser.bridge;

import dev.unifiedmc.geyser.BedrockDiagnostic;
import java.nio.file.Path;
import java.util.List;

/** Result of copying generated artifacts into a Geyser data directory. */
public record GeyserBridgeResult(
        boolean deployed, List<Path> installedFiles, List<BedrockDiagnostic> diagnostics) {
    public GeyserBridgeResult {
        installedFiles = List.copyOf(installedFiles);
        diagnostics = List.copyOf(diagnostics);
    }
}
