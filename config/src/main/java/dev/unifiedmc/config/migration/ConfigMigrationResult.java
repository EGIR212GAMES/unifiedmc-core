package dev.unifiedmc.config.migration;

import com.fasterxml.jackson.databind.JsonNode;
import dev.unifiedmc.config.validation.ConfigDiagnostic;
import java.util.List;

/** Result of migrating a raw configuration tree. */
public record ConfigMigrationResult(
        JsonNode tree, List<ConfigDiagnostic> diagnostics, boolean migrated) {
    public ConfigMigrationResult {
        diagnostics = List.copyOf(diagnostics);
    }
}
