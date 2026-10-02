package dev.unifiedmc.examples.uapi;

import static org.junit.jupiter.api.Assertions.*;

import dev.unifiedmc.uapi.compat.CompatibilityReport;
import org.junit.jupiter.api.Test;

class ExampleModAdapterTest {
    @Test
    void exposesContentAndIndependentRepresentations() {
        ExampleModAdapter adapter = new ExampleModAdapter();
        CompatibilityReport report = adapter.compatibility(ExampleModAdapter.targetContext());

        assertEquals(CompatibilityReport.Status.SUPPORTED, report.status());
        var content = adapter.content(ExampleModAdapter.targetContext());
        assertEquals(1, content.blocks().size());
        assertEquals(1, content.items().size());
        assertEquals(1, content.entities().size());
        assertEquals(1, content.recipes().size());

        var representations =
                adapter.representations(ExampleModAdapter.targetContext()).representations();
        assertEquals(4, representations.size());
        assertTrue(
                representations.values().stream()
                        .allMatch(r -> r.javaRepresentation().isPresent()));
        assertTrue(
                representations.values().stream()
                        .allMatch(r -> r.bedrockRepresentation().isPresent()));
    }

    @Test
    void doesNotClaimUnsupportedTarget() {
        ExampleModAdapter adapter = new ExampleModAdapter();
        var context =
                new dev.unifiedmc.uapi.compat.CompatibilityContext(
                        new dev.unifiedmc.version.GameVersion("26.3"),
                        dev.unifiedmc.mod.ModLoader.FABRIC,
                        true,
                        true,
                        true,
                        true,
                        java.util.Set.of(dev.unifiedmc.uapi.Capability.BLOCKS));

        assertEquals(
                CompatibilityReport.Status.UNSUPPORTED, adapter.compatibility(context).status());
    }
}
