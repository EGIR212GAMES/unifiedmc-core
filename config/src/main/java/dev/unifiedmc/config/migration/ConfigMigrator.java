package dev.unifiedmc.config.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.unifiedmc.config.ConfigSchemaVersion;
import dev.unifiedmc.config.DefaultConfigurationFactory;
import dev.unifiedmc.config.io.TomlConfigurationCodec;
import dev.unifiedmc.config.validation.ConfigDiagnostic;
import java.util.ArrayList;
import java.util.List;

/** Explicit, deterministic config schema migrations. */
public final class ConfigMigrator {
    private final TomlConfigurationCodec codec;

    public ConfigMigrator(TomlConfigurationCodec codec) {
        this.codec = codec;
    }

    public ConfigMigrationResult migrate(JsonNode input) {
        JsonNode schemaNode = input.get("schema");
        if (schemaNode == null && !input.has("directories")) {
            return new ConfigMigrationResult(
                    input,
                    List.of(
                            new ConfigDiagnostic(
                                    ConfigDiagnostic.Severity.ERROR,
                                    "",
                                    "schema",
                                    "unifiedmc-1 or unifiedmc-2",
                                    "<missing>",
                                    "Add schema = \"unifiedmc-2\" or provide a legacy v1 [directories] section for migration.")),
                    false);
        }
        String schema = schemaNode == null ? "unifiedmc-1" : schemaNode.asText();
        if (ConfigSchemaVersion.V2.value().equalsIgnoreCase(schema)) {
            return new ConfigMigrationResult(input, List.of(), false);
        }
        if (!ConfigSchemaVersion.V1.value().equalsIgnoreCase(schema)) {
            return new ConfigMigrationResult(
                    input,
                    List.of(
                            new ConfigDiagnostic(
                                    ConfigDiagnostic.Severity.ERROR,
                                    "",
                                    "schema",
                                    "unifiedmc-1 or unifiedmc-2",
                                    schema,
                                    "Use a supported schema version or generate a fresh configuration.")),
                    false);
        }

        ObjectNode migrated =
                (ObjectNode)
                        codec.mergeDefaults(toTree(DefaultConfigurationFactory.create()), input);
        ObjectNode directories =
                input.has("directories") && input.get("directories").isObject()
                        ? (ObjectNode) input.get("directories")
                        : null;
        copyIfPresent(directories, "fabric_mods", migrated.withObject("mods"), "fabric-directory");
        copyIfPresent(directories, "forge_mods", migrated.withObject("mods"), "forge-directory");
        copyIfPresent(
                directories, "neoforge_mods", migrated.withObject("mods"), "neoforge-directory");
        copyIfPresent(directories, "backends", migrated.withObject("storage"), "backend-directory");
        copyIfPresent(directories, "packs", migrated.withObject("storage"), "pack-directory");
        migrated.remove("directories");
        migrated.put("schema", ConfigSchemaVersion.V2.value());

        List<ConfigDiagnostic> notices = new ArrayList<>();
        notices.add(
                new ConfigDiagnostic(
                        ConfigDiagnostic.Severity.WARNING,
                        "schema",
                        "schema",
                        ConfigSchemaVersion.V2.value(),
                        ConfigSchemaVersion.V1.value(),
                        "Configuration was migrated in memory; validation is non-mutating. Persist v2 with an explicit migration/write operation."));
        return new ConfigMigrationResult(migrated, notices, true);
    }

    private ObjectNode toTree(dev.unifiedmc.config.CoreConfiguration config) {
        try {
            return (ObjectNode) codec.readTree(codec.write(config));
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to serialize default configuration for migration", exception);
        }
    }

    private void copyIfPresent(
            ObjectNode source, String oldName, ObjectNode destination, String newName) {
        if (source == null) {
            return;
        }
        JsonNode value = source.get(oldName);
        if (value != null) {
            destination.set(newName, value);
        }
    }
}
