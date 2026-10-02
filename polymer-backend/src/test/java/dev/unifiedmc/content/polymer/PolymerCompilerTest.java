package dev.unifiedmc.content.polymer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.unifiedmc.content.ContentCompilationContext;
import dev.unifiedmc.content.ContentCompilationStatus;
import dev.unifiedmc.content.compiler.ContentCompilationPipeline;
import dev.unifiedmc.examples.uapi.ExampleModAdapter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.junit.jupiter.api.Test;

class PolymerCompilerTest {
    @Test
    void examplePackIsPartialWhenBehaviorHasNoSafePolymerMapping() throws Exception {
        Path output = Files.createTempDirectory("polymer-test-");
        var result = compile(output);

        assertEquals(ContentCompilationStatus.PARTIAL, result.result().status());
        assertTrue(
                result.diagnostics().stream()
                        .anyMatch(d -> d.message().contains("cannot be represented")));
        assertEquals(10, result.ir().definitions().size());
        assertEquals(9, result.result().registrations().size());
    }

    @Test
    void outputIsDeterministic() throws Exception {
        Path one = Files.createTempDirectory("polymer-one-");
        Path two = Files.createTempDirectory("polymer-two-");
        compile(one);
        compile(two);

        var first = files(one);
        var second = files(two);
        assertEquals(first.keySet(), second.keySet());
        for (String path : first.keySet()) {
            assertArrayEquals(first.get(path), second.get(path), path);
        }
    }

    @Test
    void capabilityDetectorRejectsNetworking() {
        var detector = new PolymerCapabilityDetector();
        assertFalse(detector.supports(dev.unifiedmc.uapi.Capability.NETWORKING));
        assertTrue(
                detector.unsupported(java.util.Set.of(dev.unifiedmc.uapi.Capability.NETWORKING))
                        .contains(dev.unifiedmc.uapi.Capability.NETWORKING));
    }

    private static dev.unifiedmc.content.compiler.ContentPipelineResult compile(Path output) {
        return new ContentCompilationPipeline()
                .compile(
                        new ExampleModAdapter(),
                        new ContentCompilationContext(ExampleModAdapter.targetContext(), output),
                        new PolymerBackend());
    }

    private static java.util.Map<String, byte[]> files(Path root) throws Exception {
        try (var stream = Files.walk(root)) {
            var result = new java.util.LinkedHashMap<String, byte[]>();
            for (Path path :
                    stream.filter(Files::isRegularFile)
                            .sorted(Comparator.comparing(Path::toString))
                            .toList()) {
                result.put(root.relativize(path).toString(), Files.readAllBytes(path));
            }
            return result;
        }
    }
}
