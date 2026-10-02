package dev.unifiedmc.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LegacyRuntimeCatalogTest {
    @Test
    void exposesOnlyExperimentalLegacyCoordinates() {
        var entries = new LegacyRuntimeCatalog().entries();
        assertTrue(entries.stream().anyMatch(entry -> entry.game().value().equals("1.12.2")));
        assertTrue(entries.stream().anyMatch(entry -> entry.game().value().equals("1.7.10")));
        assertEquals(2, entries.size());
    }
}
