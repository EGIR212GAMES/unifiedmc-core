package dev.unifiedmc.mod.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.unifiedmc.config.model.ModConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultModManagerIntegrationTest {
    @TempDir Path tempDir;

    @Test
    void detectsDuplicateModAcrossLoaderDirectories() throws Exception {
        Path fabric = Files.createDirectory(tempDir.resolve("FabricMods"));
        Path forge = Files.createDirectory(tempDir.resolve("ForgeMods"));
        Path neo = Files.createDirectory(tempDir.resolve("NeoForgeMods"));
        Files.createFile(fabric.resolve("example-1.0.jar"));
        Files.createFile(forge.resolve("example-1.1.jar"));
        ModConfig config =
                new ModConfig(
                        fabric.toString(), forge.toString(), neo.toString(), false, true, true);

        DefaultModManager manager = new DefaultModManager(config);
        manager.refresh();

        assertEquals(ModScanResult.Status.DUPLICATE, manager.lastScan().status());
        assertTrue(
                manager.lastScan().diagnostics().stream()
                        .anyMatch(value -> value.contains("Duplicate mod id")));
    }

    @Test
    void createsMissingManagedDirectories() {
        ModConfig config =
                new ModConfig(
                        tempDir.resolve("FabricMods").toString(),
                        tempDir.resolve("ForgeMods").toString(),
                        tempDir.resolve("NeoForgeMods").toString(),
                        false,
                        true,
                        true);
        DefaultModManager manager = new DefaultModManager(config);

        manager.refresh();

        assertTrue(Files.isDirectory(tempDir.resolve("FabricMods")));
        assertEquals(ModScanResult.Status.EMPTY, manager.lastScan().status());
    }
}
