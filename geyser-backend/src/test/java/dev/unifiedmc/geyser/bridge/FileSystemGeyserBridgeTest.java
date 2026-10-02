package dev.unifiedmc.geyser.bridge;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.unifiedmc.content.ContentCompilationContext;
import dev.unifiedmc.examples.uapi.ExampleModAdapter;
import dev.unifiedmc.geyser.compiler.BedrockCompiler;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

class FileSystemGeyserBridgeTest {
    @Test
    void deploysMappingsAndPackIntoGeyserDirectories() throws Exception {
        var adapter = new ExampleModAdapter();
        var ctx = ExampleModAdapter.targetContext();
        var ir =
                new dev.unifiedmc.content.compiler.ContentIrNormalizer()
                        .normalize(adapter.adapterId(), adapter.modId(), adapter.content(ctx), ctx);
        var out = Files.createTempDirectory("bedrock-out-");
        var result =
                new BedrockCompiler().compileDetailed(ir, new ContentCompilationContext(ctx, out));
        var geyser = Files.createTempDirectory("geyser-data-");
        var bridge = new FileSystemGeyserBridge();
        var deployed =
                bridge.deploy(
                        result,
                        new GeyserBridge.GeyserDeploymentTarget(
                                geyser.resolve("custom_mappings"), geyser.resolve("packs"), true));
        assertTrue(deployed.deployed());
        assertTrue(Files.exists(geyser.resolve("custom_mappings/blocks.json")));
        try (var files = Files.list(geyser.resolve("packs"))) {
            assertTrue(files.anyMatch(p -> p.getFileName().toString().endsWith(".mcpack")));
        }
    }
}
