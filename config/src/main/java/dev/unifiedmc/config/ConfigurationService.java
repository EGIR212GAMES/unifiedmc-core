package dev.unifiedmc.config;

import com.fasterxml.jackson.databind.JsonNode;
import dev.unifiedmc.config.io.TomlConfigurationCodec;
import dev.unifiedmc.config.migration.ConfigMigrationResult;
import dev.unifiedmc.config.migration.ConfigMigrator;
import dev.unifiedmc.config.validation.ConfigDiagnostic;
import dev.unifiedmc.config.validation.ConfigSchemaValidator;
import dev.unifiedmc.config.validation.ConfigValidationResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** High-level load, validate, migrate and generate operations. */
public final class ConfigurationService {
    private final TomlConfigurationCodec codec;
    private final ConfigMigrator migrator;
    private final ConfigSchemaValidator validator;

    public ConfigurationService() {
        codec = new TomlConfigurationCodec();
        migrator = new ConfigMigrator(codec);
        validator = new ConfigSchemaValidator();
    }

    public CoreConfiguration load(Path path) {
        ConfigLoadResult result = inspect(path);
        List<ConfigDiagnostic> errors =
                result.diagnostics().stream()
                        .filter(
                                diagnostic ->
                                        diagnostic.severity() == ConfigDiagnostic.Severity.ERROR)
                        .toList();
        if (!errors.isEmpty()) {
            throw new ConfigException("Configuration validation failed: " + path, errors);
        }
        return result.configuration();
    }

    public ConfigLoadResult inspect(Path path) {
        try {
            JsonNode raw = codec.readTree(path);
            ConfigMigrationResult migration = migrator.migrate(raw);
            List<ConfigDiagnostic> diagnostics = new ArrayList<>(migration.diagnostics());
            diagnostics.addAll(validator.validateRawTree(migration.tree(), path.toString()));
            CoreConfiguration configuration =
                    codec.bind(
                            codec.mergeDefaults(
                                    codec.readTree(
                                            codec.write(DefaultConfigurationFactory.create())),
                                    migration.tree()));
            ConfigValidationResult semantic = validator.validate(configuration, path.toString());
            diagnostics.addAll(semantic.diagnostics());
            return new ConfigLoadResult(
                    configuration, List.copyOf(diagnostics), migration.migrated());
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ConfigException configException) {
                return new ConfigLoadResult(null, configException.diagnostics(), false);
            }
            List<ConfigDiagnostic> diagnostics =
                    exception instanceof com.fasterxml.jackson.core.JacksonException
                            ? codec.toDiagnostics(exception, path.toString())
                            : List.of(
                                    new ConfigDiagnostic(
                                            ConfigDiagnostic.Severity.ERROR,
                                            path.toString(),
                                            "$",
                                            "readable TOML file",
                                            exception.getMessage() == null
                                                    ? exception.getClass().getSimpleName()
                                                    : exception.getMessage(),
                                            "Ensure the file exists, is UTF-8 text, and is readable by the UnifiedMC process."));
            return new ConfigLoadResult(null, diagnostics, false);
        }
    }

    public void generate(Path path, boolean overwrite) throws IOException {
        if (Files.exists(path) && !overwrite) {
            throw new ConfigException(
                    "Configuration already exists: " + path,
                    List.of(
                            new ConfigDiagnostic(
                                    ConfigDiagnostic.Severity.ERROR,
                                    path.toString(),
                                    "file",
                                    "missing unless overwrite=true",
                                    "file exists",
                                    "Choose another output path or rerun with the CLI --force option.")));
        }
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(path, codec.write(DefaultConfigurationFactory.create()));
    }

    public record ConfigLoadResult(
            CoreConfiguration configuration, List<ConfigDiagnostic> diagnostics, boolean migrated) {
        public ConfigLoadResult {
            diagnostics = List.copyOf(diagnostics);
        }

        public boolean valid() {
            return diagnostics.stream()
                    .noneMatch(d -> d.severity() == ConfigDiagnostic.Severity.ERROR);
        }
    }
}
