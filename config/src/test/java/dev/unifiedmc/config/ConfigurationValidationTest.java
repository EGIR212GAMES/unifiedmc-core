package dev.unifiedmc.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigurationValidationTest {
    @TempDir Path temp;

    @Test
    void invalidPortProducesActionableDiagnostic() throws Exception {
        ConfigurationService service = new ConfigurationService();
        Path path = temp.resolve("invalid.toml");
        service.generate(path, false);
        Files.writeString(
                path, Files.readString(path).replace("java-port = 25565", "java-port = 70000"));

        ConfigurationService.ConfigLoadResult result = service.inspect(path);

        assertFalse(result.valid());
        assertTrue(
                result.diagnostics().stream()
                        .anyMatch(
                                d ->
                                        d.property().equals("server.java-port")
                                                && d.expected().contains("65535")));
    }

    @Test
    void unknownPropertyReportsPropertyPath() throws Exception {
        Path path = temp.resolve("unknown.toml");
        Files.writeString(
                path,
                """
                schema = "unifiedmc-2"
                [server]
                name = "x"
                motd = "y"
                bind = "0.0.0.0"
                java-port = 25565
                bedrock-port = 19132
                [server.experimental-typo]
                enabled = true
                """);

        ConfigurationService.ConfigLoadResult result = new ConfigurationService().inspect(path);

        assertFalse(result.valid());
        assertTrue(
                result.diagnostics().stream()
                        .anyMatch(
                                d ->
                                        d.property().contains("experimental-typo")
                                                || d.property().contains("experimental")));
    }

    @Test
    void missingSchemaIsRejectedUnlessLegacyLayoutIsRecognized() throws Exception {
        Path path = temp.resolve("missing-schema.toml");
        Files.writeString(path, "[server]\nname = \"x\"\n");

        ConfigurationService.ConfigLoadResult result = new ConfigurationService().inspect(path);

        assertFalse(result.valid());
        assertTrue(result.diagnostics().stream().anyMatch(d -> d.property().equals("schema")));
    }

    @Test
    void malformedTomlReportsSafeDiagnostic() throws Exception {
        Path path = temp.resolve("bad.toml");
        Files.writeString(path, "server = [not valid toml");

        ConfigurationService.ConfigLoadResult result = new ConfigurationService().inspect(path);

        assertFalse(result.valid());
        assertTrue(result.diagnostics().stream().allMatch(d -> !d.actual().contains("password=")));
    }
}
