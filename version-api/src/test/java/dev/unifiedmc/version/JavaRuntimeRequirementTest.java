package dev.unifiedmc.version;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JavaRuntimeRequirementTest {
    @Test
    void exactRequirementRejectsWrongJavaMajor() {
        var requirement = new JavaRuntimeRequirement(new GameVersion("26.3"), 25, 25, "test");

        assertTrue(requirement.accepts(25));
        assertFalse(requirement.accepts(21));
        assertFalse(requirement.accepts(26));
    }
}
