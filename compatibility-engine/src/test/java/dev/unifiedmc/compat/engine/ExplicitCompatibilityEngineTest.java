package dev.unifiedmc.compat.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.unifiedmc.compat.CompatibilityLevel;
import dev.unifiedmc.compat.CompatibilityRequest;
import dev.unifiedmc.mod.ModDescriptor;
import dev.unifiedmc.mod.ModId;
import dev.unifiedmc.mod.ModLoaderType;
import dev.unifiedmc.version.GameVersion;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import dev.unifiedmc.version.RuntimeVersion;
import org.junit.jupiter.api.Test;

class ExplicitCompatibilityEngineTest {
    @Test
    void reportsUnsupportedInsteadOfSilentFallback() {
        var mod = new ModDescriptor(new ModId("example"), "1.0", ModLoaderType.FABRIC, "26.1");
        var target =
                new RuntimeVersion(
                        new GameVersion("26.1"),
                        "neoforge",
                        "pending",
                        new JavaRuntimeRequirement(new GameVersion("26.1"), 25, 25, "test"));

        var result =
                new ExplicitCompatibilityEngine().evaluate(new CompatibilityRequest(mod, target));

        assertEquals(CompatibilityLevel.UNSUPPORTED, result.level());
    }
}
