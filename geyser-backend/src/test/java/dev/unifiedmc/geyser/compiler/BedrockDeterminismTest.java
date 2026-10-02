package dev.unifiedmc.geyser.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.unifiedmc.content.ContentCompilationContext;
import dev.unifiedmc.examples.uapi.ExampleModAdapter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Comparator;
import org.junit.jupiter.api.Test;

class BedrockDeterminismTest {
    @Test
    void sameInputProducesSameArtifacts() throws Exception {
        Path one = Files.createTempDirectory("bedrock-one-");
        Path two = Files.createTempDirectory("bedrock-two-");
        compile(one);
        compile(two);
        assertEquals(digestTree(one), digestTree(two));
    }

    private static void compile(Path output) {
        var adapter = new ExampleModAdapter();
        var context = ExampleModAdapter.targetContext();
        var ir =
                new dev.unifiedmc.content.compiler.ContentIrNormalizer()
                        .normalize(
                                adapter.adapterId(),
                                adapter.modId(),
                                adapter.content(context),
                                context);
        new BedrockCompiler().compileDetailed(ir, new ContentCompilationContext(context, output));
    }

    private static java.util.Map<String, String> digestTree(Path root) throws Exception {
        try (var stream = Files.walk(root)) {
            return stream.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(
                            java.util.stream.Collectors.toMap(
                                    p -> root.relativize(p).toString(),
                                    BedrockDeterminismTest::sha256,
                                    (a, b) -> a,
                                    java.util.LinkedHashMap::new));
        }
    }

    private static String sha256(Path file) {
        try {
            return java.util.HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
