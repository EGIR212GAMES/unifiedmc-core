package dev.unifiedmc.mod.manager;

import static org.junit.jupiter.api.Assertions.*;

import dev.unifiedmc.config.model.ModConfig;
import dev.unifiedmc.mod.ModCompatibilityStatus;
import dev.unifiedmc.mod.ModLoader;
import dev.unifiedmc.version.GameVersion;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import dev.unifiedmc.version.RuntimeVersion;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultModManagerMetadataTest {
    @TempDir Path tempDir;

    private ModConfig config(Path fabric, Path forge, Path neo) {
        return new ModConfig(
                fabric.toString(), forge.toString(), neo.toString(), false, true, true);
    }

    private RuntimeVersion neo263() {
        return new RuntimeVersion(
                new GameVersion("26.3"),
                "neoforge",
                "26.3",
                new JavaRuntimeRequirement(new GameVersion("26.3"), 25, 25, "test"));
    }

    @Test
    void readsFabricMetadataWithoutLoadingClasses() throws Exception {
        Path fabric = Files.createDirectories(tempDir.resolve("FabricMods"));
        Path forge = Files.createDirectories(tempDir.resolve("ForgeMods"));
        Path neo = Files.createDirectories(tempDir.resolve("NeoForgeMods"));
        ModTestFixtures.jar(
                fabric,
                "example-1.0.jar",
                "fabric.mod.json",
                ModTestFixtures.fabric("example", "1.0.0", "~26.3", "\"fabric-api\": \"*\""));
        ModTestFixtures.jar(
                fabric,
                "fabric-api-1.0.jar",
                "fabric.mod.json",
                ModTestFixtures.fabric("fabric-api", "1.0.0", "*", ""));

        DefaultModManager manager =
                new DefaultModManager(
                        config(fabric, forge, neo),
                        List.of(neo263()),
                        new ArchiveMetadataReader(),
                        new MetadataFirstCompatibilityCalculator());
        manager.refresh();

        assertEquals(ModScanResult.Status.READY, manager.lastScan().status());
        var example =
                manager.analyses().stream()
                        .filter(a -> a.metadata().id().value().equals("example"))
                        .findFirst()
                        .orElseThrow();
        assertEquals(ModLoader.FABRIC, example.metadata().loader());
        assertEquals("26.3", example.metadata().minecraftVersion().orElseThrow().value());
        assertTrue(
                example.metadata().dependencies().stream()
                        .anyMatch(d -> d.modId().equals("fabric-api")));
        assertEquals(
                ModCompatibilityStatus.UNSUPPORTED,
                example.compatibility().status(),
                "Fabric-on-NeoForge requires explicit Connector proof and must not be guessed");
    }

    @Test
    void explicitConnectorProofEnablesConnectorResult() throws Exception {
        Path fabric = Files.createDirectories(tempDir.resolve("FabricMods"));
        Path forge = Files.createDirectories(tempDir.resolve("ForgeMods"));
        Path neo = Files.createDirectories(tempDir.resolve("NeoForgeMods"));
        ModTestFixtures.jar(
                fabric,
                "connector-1.jar",
                "fabric.mod.json",
                ModTestFixtures.fabric("connector", "1.0", "~26.3", ""));
        var proof =
                new MetadataFirstCompatibilityCalculator(
                        (m, r) -> m.id().value().equals("connector"), (m, r) -> false);
        DefaultModManager manager =
                new DefaultModManager(
                        config(fabric, forge, neo),
                        List.of(neo263()),
                        new ArchiveMetadataReader(),
                        proof);
        manager.refresh();
        assertEquals(
                ModCompatibilityStatus.SUPPORTED_VIA_CONNECTOR,
                manager.analyses().getFirst().compatibility().status());
    }

    @Test
    void readsNeoForgeToml() throws Exception {
        Path fabric = Files.createDirectories(tempDir.resolve("FabricMods"));
        Path forge = Files.createDirectories(tempDir.resolve("ForgeMods"));
        Path neo = Files.createDirectories(tempDir.resolve("NeoForgeMods"));
        ModTestFixtures.jar(
                neo,
                "neo-1.jar",
                "META-INF/neoforge.mods.toml",
                ModTestFixtures.forgeToml("neo", "1.0", "[26.3,)", ""));
        DefaultModManager manager =
                new DefaultModManager(config(fabric, forge, neo), List.of(neo263()));
        manager.refresh();
        var analysis = manager.analyses().getFirst();
        assertEquals(ModLoader.NEOFORGE, analysis.metadata().loader());
        assertEquals(ModCompatibilityStatus.SUPPORTED, analysis.compatibility().status());
        assertTrue(analysis.stages().contains(dev.unifiedmc.mod.AnalysisStage.READ_METADATA));
    }

    @Test
    void readsLegacyForgeMcmodInfoAndRequiresLegacyBackend() throws Exception {
        Path fabric = Files.createDirectories(tempDir.resolve("FabricMods"));
        Path forge = Files.createDirectories(tempDir.resolve("ForgeMods"));
        Path neo = Files.createDirectories(tempDir.resolve("NeoForgeMods"));
        ModTestFixtures.jar(
                forge,
                "legacy-1.12.2.jar",
                "mcmod.info",
                ModTestFixtures.legacyForge("legacy", "1.0", "1.12.2", ""));
        DefaultModManager manager =
                new DefaultModManager(config(fabric, forge, neo), List.of(neo263()));
        manager.refresh();
        var analysis = manager.analyses().getFirst();
        assertEquals(ModLoader.FORGE, analysis.metadata().loader());
        assertEquals("1.12.2", analysis.metadata().minecraftVersion().orElseThrow().value());
        assertEquals(
                ModCompatibilityStatus.LEGACY_BACKEND_REQUIRED, analysis.compatibility().status());
    }

    @Test
    void unknownJarIsInvalidAndNeverApproved() throws Exception {
        Path fabric = Files.createDirectories(tempDir.resolve("FabricMods"));
        Path forge = Files.createDirectories(tempDir.resolve("ForgeMods"));
        Path neo = Files.createDirectories(tempDir.resolve("NeoForgeMods"));
        ModTestFixtures.jar(fabric, "mystery-1.jar", "README.txt", "not a loader descriptor");
        DefaultModManager manager =
                new DefaultModManager(config(fabric, forge, neo), List.of(neo263()));
        manager.refresh();
        assertEquals(
                ModCompatibilityStatus.INVALID,
                manager.analyses().getFirst().compatibility().status());
        assertEquals(ModScanResult.Status.INVALID, manager.lastScan().status());
    }

    @Test
    void missingDependencyIsReported() throws Exception {
        Path fabric = Files.createDirectories(tempDir.resolve("FabricMods"));
        Path forge = Files.createDirectories(tempDir.resolve("ForgeMods"));
        Path neo = Files.createDirectories(tempDir.resolve("NeoForgeMods"));
        ModTestFixtures.jar(
                neo,
                "dependent-1.jar",
                "META-INF/neoforge.mods.toml",
                ModTestFixtures.forgeToml(
                        "dependent",
                        "1.0",
                        "[26.3,)",
                        "\n[[dependencies.dependent]]\nmodId=\"missing_lib\"\nmandatory=true\nversionRange=\"[1,2)\"\nordering=\"NONE\"\nside=\"SERVER\"\n"));
        DefaultModManager manager =
                new DefaultModManager(config(fabric, forge, neo), List.of(neo263()));
        manager.refresh();
        assertTrue(
                manager.diagnosticsReport().diagnostics().stream()
                        .anyMatch(d -> d.code().equals("MISSING_DEPENDENCY")));
    }

    @Test
    void wrongDependencyVersionIsReported() throws Exception {
        Path fabric = Files.createDirectories(tempDir.resolve("FabricMods"));
        Path forge = Files.createDirectories(tempDir.resolve("ForgeMods"));
        Path neo = Files.createDirectories(tempDir.resolve("NeoForgeMods"));
        ModTestFixtures.jar(
                neo,
                "lib-1.jar",
                "META-INF/neoforge.mods.toml",
                ModTestFixtures.forgeToml("lib", "2.0", "[26.3,)", ""));
        ModTestFixtures.jar(
                neo,
                "dependent-1.jar",
                "META-INF/neoforge.mods.toml",
                ModTestFixtures.forgeToml(
                        "dependent",
                        "1.0",
                        "[26.3,)",
                        "\n[[dependencies.dependent]]\nmodId=\"lib\"\nmandatory=true\nversionRange=\"[1,2)\"\nordering=\"NONE\"\nside=\"SERVER\"\n"));
        DefaultModManager manager =
                new DefaultModManager(config(fabric, forge, neo), List.of(neo263()));
        manager.refresh();
        assertTrue(
                manager.diagnosticsReport().diagnostics().stream()
                        .anyMatch(d -> d.code().equals("WRONG_DEPENDENCY_VERSION")));
    }

    @Test
    void duplicateNamespaceIsReported() throws Exception {
        Path fabric = Files.createDirectories(tempDir.resolve("FabricMods"));
        Path forge = Files.createDirectories(tempDir.resolve("ForgeMods"));
        Path neo = Files.createDirectories(tempDir.resolve("NeoForgeMods"));
        ModTestFixtures.jar(
                neo,
                "a-1.jar",
                "META-INF/neoforge.mods.toml",
                ModTestFixtures.forgeToml("same", "1.0", "[26.3,)", ""));
        ModTestFixtures.jar(
                neo,
                "b-1.jar",
                "META-INF/neoforge.mods.toml",
                ModTestFixtures.forgeToml("same", "2.0", "[26.3,)", ""));
        DefaultModManager manager =
                new DefaultModManager(config(fabric, forge, neo), List.of(neo263()));
        manager.refresh();
        assertEquals(ModScanResult.Status.DUPLICATE, manager.lastScan().status());
        assertTrue(
                manager.diagnosticsReport().diagnostics().stream()
                        .anyMatch(d -> d.code().equals("DUPLICATE_NAMESPACE")));
    }

    @Test
    void loaderMismatchIsRejected() throws Exception {
        Path fabric = Files.createDirectories(tempDir.resolve("FabricMods"));
        Path forge = Files.createDirectories(tempDir.resolve("ForgeMods"));
        Path neo = Files.createDirectories(tempDir.resolve("NeoForgeMods"));
        ModTestFixtures.jar(
                forge,
                "fabric-1.jar",
                "fabric.mod.json",
                ModTestFixtures.fabric("fabric_in_forge", "1.0", "26.3", ""));
        DefaultModManager manager =
                new DefaultModManager(config(fabric, forge, neo), List.of(neo263()));
        manager.refresh();
        assertEquals(
                ModCompatibilityStatus.INVALID,
                manager.analyses().getFirst().compatibility().status());
        assertTrue(
                manager.diagnosticsReport().diagnostics().stream()
                        .anyMatch(d -> d.code().equals("LOADER_MISMATCH")));
    }

    @Test
    void incompatibleMinecraftVersionIsReported() throws Exception {
        Path fabric = Files.createDirectories(tempDir.resolve("FabricMods"));
        Path forge = Files.createDirectories(tempDir.resolve("ForgeMods"));
        Path neo = Files.createDirectories(tempDir.resolve("NeoForgeMods"));
        ModTestFixtures.jar(
                neo,
                "wrong-1.jar",
                "META-INF/neoforge.mods.toml",
                ModTestFixtures.forgeToml("wrong", "1.0", "[26.2,26.3)", ""));
        DefaultModManager manager =
                new DefaultModManager(config(fabric, forge, neo), List.of(neo263()));
        manager.refresh();
        assertEquals(
                ModCompatibilityStatus.UNSUPPORTED,
                manager.analyses().getFirst().compatibility().status());
        assertTrue(
                manager.diagnosticsReport().diagnostics().stream()
                        .anyMatch(d -> d.code().equals("INCOMPATIBLE_VERSION")));
    }
}
