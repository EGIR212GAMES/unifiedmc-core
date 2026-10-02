package dev.unifiedmc.geyser.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.unifiedmc.content.ContentCompilationContext;
import dev.unifiedmc.content.compiler.ContentIrNormalizer;
import dev.unifiedmc.examples.uapi.ExampleModAdapter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class BedrockGoldenTest {
    @Test
    void blockMappingMatchesGolden() throws Exception {
        var adapter = new ExampleModAdapter();
        var context = ExampleModAdapter.targetContext();
        var ir =
                new ContentIrNormalizer()
                        .normalize(
                                adapter.adapterId(),
                                adapter.modId(),
                                adapter.content(context),
                                context);
        Path output = Files.createTempDirectory("bedrock-golden-");
        var result =
                new BedrockCompiler()
                        .compileDetailed(ir, new ContentCompilationContext(context, output));
        var artifact =
                result.compileResult().artifacts().stream()
                        .filter(a -> a.relativePath().equals(Path.of("mappings", "blocks.json")))
                        .findFirst()
                        .orElseThrow();
        String actual = new String(artifact.content(), StandardCharsets.UTF_8);
        try (var stream =
                BedrockGoldenTest.class.getResourceAsStream("/golden/blocks-example.json")) {
            if (stream == null) {

                throw new IllegalStateException("Missing golden resource");
            }
            assertEquals(new String(stream.readAllBytes(), StandardCharsets.UTF_8), actual);
        }
    }
}
