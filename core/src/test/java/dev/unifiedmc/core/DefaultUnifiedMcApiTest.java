package dev.unifiedmc.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DefaultUnifiedMcApiTest {
    @Test
    void exposesStableMajorVersion() {
        assertEquals(1, new DefaultUnifiedMcApi().apiMajorVersion());
    }
}
