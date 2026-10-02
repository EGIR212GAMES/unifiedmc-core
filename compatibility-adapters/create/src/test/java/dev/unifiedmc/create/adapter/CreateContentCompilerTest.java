package dev.unifiedmc.create.adapter;

import static org.junit.jupiter.api.Assertions.*;

import dev.unifiedmc.content.ContentCompilationStatus;
import org.junit.jupiter.api.Test;

class CreateContentCompilerTest {
    @Test
    void verticalSliceIsExplicitlyPartialBecauseContraptionsAreNotImplemented() {
        var result = new CreateContentCompiler().compile(new CreateAdapter());
        assertEquals(ContentCompilationStatus.PARTIAL, result.status());
        assertTrue(
                result.limitations().stream()
                        .anyMatch(v -> v.toLowerCase().contains("contraption")));
        assertEquals(0, result.contraption().components().size());
    }
}
