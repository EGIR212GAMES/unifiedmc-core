package dev.unifiedmc.geyser.bridge;

import dev.unifiedmc.content.ContentCompileResult;
import dev.unifiedmc.geyser.BedrockCompilationResult;
import dev.unifiedmc.geyser.BedrockDiagnostic;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Installs Geyser JSON mappings and Bedrock packs without requiring a concrete Geyser host API. */
public final class FileSystemGeyserBridge implements GeyserBridge {
    @Override
    public GeyserBridgeResult deploy(
            BedrockCompilationResult result, GeyserDeploymentTarget target) {
        List<Path> installed = new ArrayList<>();
        List<BedrockDiagnostic> diagnostics = new ArrayList<>(result.diagnostics());
        if (result.status() == dev.unifiedmc.content.ContentCompilationStatus.UNSUPPORTED) {
            diagnostics.add(
                    new BedrockDiagnostic(
                            BedrockDiagnostic.Severity.ERROR,
                            "bridge",
                            "Bedrock compilation is unsupported and cannot be deployed",
                            "SUPPORTED or PARTIAL compilation",
                            result.status().name(),
                            "Fix the reported Bedrock mapping limitations before deployment."));
            return new GeyserBridgeResult(false, installed, diagnostics);
        }
        if (target.requireCustomContentEnabled()) {
            diagnostics.add(
                    new BedrockDiagnostic(
                            BedrockDiagnostic.Severity.INFO,
                            "geyser.gameplay.enable-custom-content",
                            "Geyser custom content must be enabled",
                            "true",
                            "required",
                            "Set gameplay.enable-custom-content=true and restart/reload Geyser."));
        }
        try {
            Files.createDirectories(target.customMappingsDirectory());
            Files.createDirectories(target.packsDirectory());
            for (ContentCompileResult.EmittedArtifact artifact :
                    result.compileResult().artifacts()) {
                String raw = artifact.relativePath().toString().replace('\\', '/');
                Path destination;
                if (raw.startsWith("mappings/")) {
                    destination =
                            target.customMappingsDirectory()
                                    .resolve(raw.substring("mappings/".length()));
                } else if (raw.startsWith("resourcepack/") && raw.endsWith(".mcpack")) {
                    destination = target.packsDirectory().resolve(Path.of(raw).getFileName());
                } else {
                    continue;
                }
                Path normalizedRoot = destination.getParent().toAbsolutePath().normalize();
                Files.createDirectories(normalizedRoot);
                Files.write(destination, artifact.content());
                installed.add(destination);
            }
            return new GeyserBridgeResult(true, installed, diagnostics);
        } catch (IOException e) {
            diagnostics.add(
                    new BedrockDiagnostic(
                            BedrockDiagnostic.Severity.ERROR,
                            "bridge.filesystem",
                            "Failed to deploy Geyser artifacts",
                            "writable Geyser data directories",
                            e.getClass().getSimpleName(),
                            "Fix filesystem permissions and retry."));
            return new GeyserBridgeResult(false, installed, diagnostics);
        }
    }
}
