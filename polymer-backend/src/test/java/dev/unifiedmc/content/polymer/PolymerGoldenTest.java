package dev.unifiedmc.content.polymer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.unifiedmc.content.ContentCompilationContext;
import dev.unifiedmc.content.compiler.ContentCompilationPipeline;
import dev.unifiedmc.examples.uapi.ExampleModAdapter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PolymerGoldenTest {
    @Test
    void manifestMatchesGolden() throws Exception {
        Path output = Files.createTempDirectory("polymer-golden-");
        new ContentCompilationPipeline()
                .compile(
                        new ExampleModAdapter(),
                        new ContentCompilationContext(ExampleModAdapter.targetContext(), output),
                        new PolymerBackend());
        assertEquals(
                golden("/golden/example/unifiedmc-manifest.json"),
                Files.readString(
                        output.resolve("unifiedmc-manifest.json"), StandardCharsets.UTF_8));
    }

    @Test
    void blockArtifactMatchesGolden() throws Exception {
        Path output = Files.createTempDirectory("polymer-golden-block-");
        new ContentCompilationPipeline()
                .compile(
                        new ExampleModAdapter(),
                        new ContentCompilationContext(ExampleModAdapter.targetContext(), output),
                        new PolymerBackend());
        assertEquals(
                golden("/golden/example/copper_block.json"),
                Files.readString(
                        output.resolve("content/block/example/copper_block.json"),
                        StandardCharsets.UTF_8));
    }

    private static String golden(String resource) throws Exception {
        try (var stream = PolymerGoldenTest.class.getResourceAsStream(resource)) {
            if (stream == null) {

                throw new IllegalStateException("Missing golden resource: " + resource);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
