package dev.unifiedmc.content.ir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.unifiedmc.content.compiler.ContentIrNormalizer;
import dev.unifiedmc.examples.uapi.ExampleModAdapter;
import org.junit.jupiter.api.Test;

class ContentIrTest {
    @Test
    void normalizerProducesStableOrdering() {
        var adapter = new ExampleModAdapter();
        var bundle = adapter.content(ExampleModAdapter.targetContext());
        var doc =
                new ContentIrNormalizer()
                        .normalize(
                                adapter.adapterId(),
                                adapter.modId(),
                                bundle,
                                ExampleModAdapter.targetContext());

        assertEquals(ContentIrVersion.V1, doc.irVersion());
        assertEquals("example", doc.modId());
        assertEquals("26.3", doc.minecraftVersion().value());
        assertEquals("example:copper_block", doc.blocks().get(0).id().value());
        assertEquals("example:copper_wrench_recipe", doc.recipes().get(0).id().value());
        assertEquals(10, doc.definitions().size());
    }
}
