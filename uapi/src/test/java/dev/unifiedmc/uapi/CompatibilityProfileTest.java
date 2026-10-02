package dev.unifiedmc.uapi;

import static org.junit.jupiter.api.Assertions.*;

import dev.unifiedmc.mod.ModLoader;
import dev.unifiedmc.uapi.compat.CompatibilityContext;
import dev.unifiedmc.uapi.compat.CompatibilityEvaluator;
import dev.unifiedmc.uapi.compat.CompatibilityProfile;
import dev.unifiedmc.uapi.compat.CompatibilityReport;
import dev.unifiedmc.version.GameVersion;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompatibilityProfileTest {
    @Test
    void reportsPartialForMissingCapability() {
        CompatibilityProfile profile =
                CompatibilityProfile.builder()
                        .minecraftVersion("26.3")
                        .loader(ModLoader.NEOFORGE)
                        .serverSide(true)
                        .capabilities(Capability.BLOCKS, Capability.SERVER_SIDE)
                        .build();

        CompatibilityContext context =
                new CompatibilityContext(
                        new GameVersion("26.3"),
                        ModLoader.NEOFORGE,
                        true,
                        false,
                        false,
                        true,
                        Set.of(Capability.BLOCKS, Capability.ENTITIES));

        CompatibilityReport report = CompatibilityEvaluator.evaluate("example", profile, context);

        assertEquals(CompatibilityReport.Status.PARTIAL, report.status());
        assertTrue(report.supportedCapabilities().contains(Capability.BLOCKS));
        assertTrue(report.unsupportedCapabilities().contains(Capability.ENTITIES));
        assertFalse(report.unsupportedFeatures().isEmpty());
    }

    @Test
    void rejectsUnsupportedVersion() {
        CompatibilityProfile profile =
                CompatibilityProfile.builder()
                        .minecraftVersion("26.3")
                        .loader(ModLoader.NEOFORGE)
                        .serverSide(true)
                        .build();
        CompatibilityReport report =
                CompatibilityEvaluator.evaluate(
                        "example",
                        profile,
                        CompatibilityContext.server(new GameVersion("26.2"), ModLoader.NEOFORGE));

        assertEquals(CompatibilityReport.Status.UNSUPPORTED, report.status());
    }

    @Test
    void requiresCapabilityFlagsForRepresentationDeclarations() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new CompatibilityProfile(
                                Set.of(),
                                Set.of(new GameVersion("26.3")),
                                Set.of(ModLoader.NEOFORGE),
                                false,
                                true,
                                false,
                                false,
                                java.util.List.of()));
    }
}
