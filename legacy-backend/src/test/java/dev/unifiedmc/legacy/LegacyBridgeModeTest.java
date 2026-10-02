package dev.unifiedmc.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LegacyBridgeModeTest {
    @Test
    void isolatedWorldIsExplicitDefaultBoundary() {
        assertEquals(LegacyBridgeMode.ISOLATED_WORLD, LegacyBridgeMode.valueOf("ISOLATED_WORLD"));
    }
}
