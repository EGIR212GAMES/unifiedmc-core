package dev.unifiedmc.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigurationServiceTest {
    @TempDir Path temp;

    @Test
    void generatedConfigurationLoadsAndValidates() throws Exception {
        ConfigurationService service = new ConfigurationService();
        Path config = temp.resolve("unifiedmc.toml");
        service.generate(config, false);

        ConfigurationService.ConfigLoadResult result = service.inspect(config);

        assertTrue(result.valid(), () -> result.diagnostics().toString());
        assertEquals("unifiedmc-2", result.configuration().schema());
        assertEquals("26.3", result.configuration().runtime().primaryVersion());
    }

    @Test
    void legacyDirectoryLayoutMigratesInMemory() throws Exception {
        String legacy =
                """
                schema = "unifiedmc-1"
                [directories]
                fabric_mods = "./legacy-fabric"
                forge_mods = "./legacy-forge"
                neoforge_mods = "./legacy-neoforge"
                backends = "./legacy-backends"
                packs = "./legacy-packs"
                """;
        Path config = temp.resolve("legacy.toml");
        Files.writeString(config, legacy);

        ConfigurationService.ConfigLoadResult result = new ConfigurationService().inspect(config);

        assertTrue(result.valid(), () -> result.diagnostics().toString());
        assertTrue(result.migrated());
        assertEquals("./legacy-fabric", result.configuration().mods().fabricDirectory());
        assertTrue(
                result.diagnostics().stream().anyMatch(d -> d.severity().name().equals("WARNING")));
    }
}
