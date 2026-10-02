package dev.unifiedmc.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JavaRuntimeCompatibilityTest {
    private final RuntimeJavaCompatibility catalog = RuntimeJavaCompatibility.officialBaseline();

    @Test
    void coreAnd26xUseJava25() {
        assertEquals(25, catalog.requirementFor("26.3").orElseThrow().minimumJava());
        assertEquals(25, catalog.requirementFor("26.2").orElseThrow().minimumJava());
        assertEquals(25, catalog.requirementFor("26.1").orElseThrow().minimumJava());
    }

    @Test
    void legacyProfilesKeepTheirOwnJavaBaselines() {
        assertEquals(21, catalog.requirementFor("1.21.1").orElseThrow().minimumJava());
        assertEquals(17, catalog.requirementFor("1.20.1").orElseThrow().minimumJava());
        assertEquals(8, catalog.requirementFor("1.12.2").orElseThrow().minimumJava());
    }

    @Test
    void onePointSevenTenIsNotGuessed() {
        assertTrue(catalog.requirementFor("1.7.10").isEmpty());
    }
}
