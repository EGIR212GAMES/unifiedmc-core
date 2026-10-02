package dev.unifiedmc.config.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;
import dev.unifiedmc.config.CoreConfiguration;
import dev.unifiedmc.config.logging.ConfigurationRedactor;
import dev.unifiedmc.config.validation.ConfigDiagnostic;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** TOML parser/serializer for the versioned UnifiedMC schema. */
public final class TomlConfigurationCodec {
    private final ObjectMapper mapper;

    public TomlConfigurationCodec() {
        mapper = new ObjectMapper(new TomlFactory());
        mapper.findAndRegisterModules();
        mapper.setPropertyNamingStrategy(
                com.fasterxml.jackson.databind.PropertyNamingStrategies.KEBAB_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
    }

    public JsonNode readTree(Path path) throws IOException {
        return mapper.readTree(Files.readString(path));
    }

    public JsonNode readTree(String toml) throws IOException {
        return mapper.readTree(toml);
    }

    public CoreConfiguration bind(JsonNode node) throws JsonProcessingException {
        return mapper.treeToValue(node, CoreConfiguration.class);
    }

    public String write(CoreConfiguration configuration) throws JsonProcessingException {
        return mapper.writeValueAsString(configuration);
    }

    public List<ConfigDiagnostic> toDiagnostics(Exception exception, String sourcePath) {
        List<ConfigDiagnostic> diagnostics = new ArrayList<>();
        if (exception
                instanceof
                com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException unknown) {
            String property = unknown.getPropertyName();
            String path = buildPath(unknown.getPath(), property);
            diagnostics.add(
                    new ConfigDiagnostic(
                            ConfigDiagnostic.Severity.ERROR,
                            sourcePath,
                            path,
                            "a property defined by unifiedmc-2 schema",
                            ConfigurationRedactor.redact(path, property),
                            "Remove the unknown property or use the documented schema spelling."));
            return diagnostics;
        }
        if (exception instanceof com.fasterxml.jackson.databind.JsonMappingException mapping) {
            String property = buildPath(mapping.getPath(), "value");
            String actual =
                    exception.getMessage() == null
                            ? exception.getClass().getSimpleName()
                            : exception.getMessage();
            diagnostics.add(
                    new ConfigDiagnostic(
                            ConfigDiagnostic.Severity.ERROR,
                            sourcePath,
                            property,
                            "a value compatible with the documented property type",
                            ConfigurationRedactor.redact(property, sanitize(actual)),
                            "Correct the property type/value and re-run config validate."));
            return diagnostics;
        }
        String actual =
                exception.getMessage() == null
                        ? exception.getClass().getSimpleName()
                        : exception.getMessage();
        diagnostics.add(
                new ConfigDiagnostic(
                        ConfigDiagnostic.Severity.ERROR,
                        sourcePath,
                        "$",
                        "valid TOML matching unifiedmc-2",
                        sanitize(actual),
                        "Check TOML syntax near the reported line/column and compare the file with the generated default configuration."));
        return diagnostics;
    }

    private String buildPath(
            List<com.fasterxml.jackson.databind.JsonMappingException.Reference> refs,
            String property) {
        StringBuilder path = new StringBuilder();
        for (var ref : refs) {
            if (ref.getFieldName() != null) {
                if (!path.isEmpty()) {

                    path.append('.');
                }
                path.append(ref.getFieldName());
            }
        }
        if (!path.isEmpty()) {

            path.append('.');
        }
        path.append(property);
        return path.toString();
    }

    private String sanitize(String message) {
        return message.replaceAll(
                "(?i)(password|token|secret|private[-_ ]?key)\\s*[=:]\\s*[^,;\\s]+",
                "$1=<redacted>");
    }

    public JsonNode mergeDefaults(JsonNode defaults, JsonNode input) {
        if (!defaults.isObject() || !input.isObject()) {
            return input;
        }
        ObjectNode merged = ((ObjectNode) defaults).deepCopy();
        for (Map.Entry<String, JsonNode> entry : input.properties()) {
            JsonNode existing = merged.get(entry.getKey());
            JsonNode value = entry.getValue();
            if (existing != null && existing.isObject() && value.isObject()) {
                merged.set(entry.getKey(), mergeDefaults(existing, value));
            } else {
                merged.set(entry.getKey(), value);
            }
        }
        return merged;
    }
}
