package dev.unifiedmc.geyser.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.unifiedmc.content.ContentCompilationContext;
import dev.unifiedmc.content.ContentCompilationStatus;
import dev.unifiedmc.examples.uapi.ExampleModAdapter;
import dev.unifiedmc.geyser.BedrockCapability;
import dev.unifiedmc.geyser.BedrockCapabilityStatus;
import dev.unifiedmc.geyser.BedrockCompilationResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;

class BedrockCompilerIntegrationTest {
    @Test
    void exampleProducesExplicitMappingsAndDiagnostics() throws Exception {
        Path out = Files.createTempDirectory("geyser-bedrock-");
        BedrockCompilationResult result = new BedrockBackendFacade().compile(out);

        assertEquals(ContentCompilationStatus.PARTIAL, result.status());
        assertEquals(
                BedrockCapabilityStatus.SUPPORTED,
                result.capabilityReport().status(BedrockCapability.BLOCK_REGISTRATION));
        assertEquals(
                BedrockCapabilityStatus.SUPPORTED,
                result.capabilityReport().status(BedrockCapability.BLOCK_VISUAL));
        assertEquals(
                BedrockCapabilityStatus.PARTIAL,
                result.capabilityReport().status(BedrockCapability.BLOCK_INTERACTION));
        assertEquals(
                BedrockCapabilityStatus.PARTIAL,
                result.capabilityReport().status(BedrockCapability.ITEM_REGISTRATION));
        assertEquals(
                BedrockCapabilityStatus.UNSUPPORTED,
                result.capabilityReport().status(BedrockCapability.GEYSER_EXTENSION));

        String blocks =
                Files.readString(out.resolve("mappings/blocks.json"), StandardCharsets.UTF_8);
        assertTrue(blocks.contains("example:copper_block"));
        assertTrue(blocks.contains("material_instances"));

        Path pack =
                Files.list(out.resolve("resourcepack"))
                        .filter(p -> p.toString().endsWith(".mcpack"))
                        .findFirst()
                        .orElseThrow();
        try (ZipInputStream zip =
                new ZipInputStream(Files.newInputStream(pack), StandardCharsets.UTF_8)) {
            boolean manifest = false;
            boolean requirements = false;
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                manifest |= entry.getName().equals("manifest.json");
                requirements |= entry.getName().equals("unifiedmc/asset-requirements.json");
            }
            assertTrue(manifest);
            assertTrue(requirements);
        }

        String diagnostics =
                Files.readString(out.resolve("diagnostics/report.json"), StandardCharsets.UTF_8);
        assertTrue(diagnostics.contains("BLOCK_INTERACTION"));
        assertTrue(diagnostics.contains("GEYSER_EXTENSION"));
    }

    private static final class BedrockBackendFacade {
        BedrockCompilationResult compile(Path output) {
            var adapter = new ExampleModAdapter();
            var context = ExampleModAdapter.targetContext();
            var ir =
                    new dev.unifiedmc.content.compiler.ContentIrNormalizer()
                            .normalize(
                                    adapter.adapterId(),
                                    adapter.modId(),
                                    adapter.content(context),
                                    context);
            var backendResult =
                    new dev.unifiedmc.geyser.compiler.BedrockCompiler()
                            .compileDetailed(ir, new ContentCompilationContext(context, output));
            for (var artifact : backendResult.compileResult().artifacts()) {
                try {
                    var path = output.resolve(artifact.relativePath()).normalize();
                    Files.createDirectories(path.getParent());
                    Files.write(path, artifact.content());
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
            return backendResult;
        }
    }
}
