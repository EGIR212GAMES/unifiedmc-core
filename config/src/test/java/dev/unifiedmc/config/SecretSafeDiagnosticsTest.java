package dev.unifiedmc.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.unifiedmc.config.logging.ConfigurationRedactor;
import org.junit.jupiter.api.Test;

class SecretSafeDiagnosticsTest {
    @Test
    void secretPropertiesAreRedacted() {
        assertEquals("<redacted>", ConfigurationRedactor.redact("security.token", "s3cr3t"));
        assertEquals(
                "/tmp/config.toml",
                ConfigurationRedactor.redact("logging.file", "/tmp/config.toml"));
    }
}
