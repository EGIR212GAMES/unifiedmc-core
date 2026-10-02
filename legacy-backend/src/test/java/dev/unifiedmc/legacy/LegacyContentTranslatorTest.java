package dev.unifiedmc.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LegacyContentTranslatorTest {
    @Test
    void doesNotPretendGenericTranslationExists() {
        var result =
                new UnsupportedLegacyContentTranslator()
                        .translate(new LegacyContentRequest("example:thing", "1.12.2", "26.3"));
        assertEquals(LegacyContentTranslationResult.Status.UNSUPPORTED, result.status());
    }
}
