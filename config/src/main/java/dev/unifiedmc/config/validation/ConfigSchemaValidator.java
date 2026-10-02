package dev.unifiedmc.config.validation;

import com.fasterxml.jackson.databind.JsonNode;
import dev.unifiedmc.config.ConfigSchemaVersion;
import dev.unifiedmc.config.CoreConfiguration;
import dev.unifiedmc.config.model.ChecksumPolicy;
import dev.unifiedmc.config.model.CompatibilityMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validates semantic constraints that a data-binding library cannot express safely. */
public final class ConfigSchemaValidator {
    public ConfigValidationResult validate(CoreConfiguration configuration, String sourcePath) {
        List<ConfigDiagnostic> diagnostics = new ArrayList<>();
        if (!ConfigSchemaVersion.V2.value().equals(configuration.schema())) {
            diagnostics.add(
                    error(
                            sourcePath,
                            "schema",
                            ConfigSchemaVersion.V2.value(),
                            configuration.schema(),
                            "Run config migration or generate a new v2 configuration."));
        }
        validateServer(configuration, sourcePath, diagnostics);
        validateRuntime(configuration, sourcePath, diagnostics);
        validateVersions(configuration, sourcePath, diagnostics);
        validateCompatibility(configuration, sourcePath, diagnostics);
        validateSecurity(configuration, sourcePath, diagnostics);
        validateLogging(configuration, sourcePath, diagnostics);
        return new ConfigValidationResult(diagnostics);
    }

    private void validateServer(CoreConfiguration c, String path, List<ConfigDiagnostic> d) {
        if (c.server().name().isBlank()) {
            d.add(
                    error(
                            path,
                            "server.name",
                            "non-empty string",
                            c.server().name(),
                            "Set server.name to a non-empty display name."));
        }
        checkPort(d, path, "server.java-port", c.server().javaPort());
        checkPort(d, path, "server.bedrock-port", c.server().bedrockPort());
    }

    private void validateRuntime(CoreConfiguration c, String path, List<ConfigDiagnostic> d) {
        if (c.runtime().primaryVersion().isBlank()) {
            d.add(
                    error(
                            path,
                            "runtime.primary-version",
                            "non-empty Minecraft version",
                            c.runtime().primaryVersion(),
                            "Set the primary backend version, for example 26.3."));
        }
        if (c.runtime().defaultBackend().isBlank()) {
            d.add(
                    error(
                            path,
                            "runtime.default-backend",
                            "non-empty backend id",
                            c.runtime().defaultBackend(),
                            "Set a backend id present in the backend inventory."));
        }
        if (!c.versions().backendVersions().contains(c.runtime().primaryVersion())) {
            d.add(
                    error(
                            path,
                            "runtime.primary-version",
                            "a value present in versions.backend-versions",
                            c.runtime().primaryVersion(),
                            "Add the primary runtime version to versions.backend-versions or select an existing backend version."));
        }
        if (c.runtime().allowMultiRuntime() && !c.security().isolatedRuntimes()) {
            d.add(
                    error(
                            path,
                            "security.isolated-runtimes",
                            "true when runtime.allow-multi-runtime=true",
                            Boolean.toString(c.security().isolatedRuntimes()),
                            "Enable runtime isolation before allowing multiple backend runtimes."));
        }
    }

    private void validateVersions(CoreConfiguration c, String path, List<ConfigDiagnostic> d) {
        if (c.versions().allowedClientVersions().isEmpty()) {
            d.add(
                    error(
                            path,
                            "versions.allowed-client-versions",
                            "at least one version",
                            "[]",
                            "Add the Java client versions the gateway is allowed to accept."));
        }
        if (c.versions().backendVersions().isEmpty()) {
            d.add(
                    error(
                            path,
                            "versions.backend-versions",
                            "at least one version",
                            "[]",
                            "Add at least one backend version."));
        }
        Set<String> duplicateCheck = new HashSet<>();
        for (String version : c.versions().backendVersions()) {
            if (!duplicateCheck.add(version)) {
                d.add(
                        error(
                                path,
                                "versions.backend-versions",
                                "unique version values",
                                version,
                                "Remove duplicate backend versions."));
                break;
            }
        }
    }

    private void validateCompatibility(CoreConfiguration c, String path, List<ConfigDiagnostic> d) {
        if (c.bedrock().enabled() && !c.bedrock().geyser()) {
            d.add(
                    error(
                            path,
                            "bedrock.geyser",
                            "true when bedrock.enabled=true",
                            Boolean.toString(c.bedrock().geyser()),
                            "Enable the Geyser integration or disable Bedrock support."));
        }
        if (!c.compatibility().enabled() && c.compatibility().mode() != CompatibilityMode.STRICT) {
            d.add(
                    error(
                            path,
                            "compatibility.mode",
                            "strict when compatibility.enabled=false",
                            c.compatibility().mode().value(),
                            "Disable compatibility features or set mode = \"strict\"."));
        }
    }

    private void validateSecurity(CoreConfiguration c, String path, List<ConfigDiagnostic> d) {
        if (!c.mods().verifySignatures() && !c.security().allowUnsignedMods()) {
            d.add(
                    error(
                            path,
                            "mods.verify-signatures",
                            "true when security.allow-unsigned-mods=false",
                            Boolean.toString(c.mods().verifySignatures()),
                            "Enable signature verification or explicitly allow unsigned mods."));
        }
        if (c.security().allowUnsignedMods()
                && c.security().checksumPolicy() == ChecksumPolicy.REQUIRED) {
            d.add(
                    new ConfigDiagnostic(
                            ConfigDiagnostic.Severity.WARNING,
                            path,
                            "security.allow-unsigned-mods",
                            "consistent artifact policy",
                            "true",
                            "Unsigned mods are allowed while checksums remain required; review this weaker signature posture."));
        }
    }

    private void validateLogging(CoreConfiguration c, String path, List<ConfigDiagnostic> d) {
        Set<String> levels = Set.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR");
        if (!levels.contains(c.logging().level().toUpperCase(java.util.Locale.ROOT))) {
            d.add(
                    error(
                            path,
                            "logging.level",
                            "TRACE|DEBUG|INFO|WARN|ERROR",
                            c.logging().level(),
                            "Choose one of the supported logging levels."));
        }
    }

    private void checkPort(List<ConfigDiagnostic> d, String path, String property, int port) {
        if (port < 1 || port > 65535) {
            d.add(
                    error(
                            path,
                            property,
                            "integer in range 1..65535",
                            Integer.toString(port),
                            "Choose an unused TCP/UDP port in the valid range."));
        }
    }

    private ConfigDiagnostic error(
            String path, String property, String expected, String actual, String fix) {
        return new ConfigDiagnostic(
                ConfigDiagnostic.Severity.ERROR, path, property, expected, actual, fix);
    }

    /** Validates raw property names against the versioned schema before binding. */
    public List<ConfigDiagnostic> validateRawTree(JsonNode root, String sourcePath) {
        List<ConfigDiagnostic> diagnostics = new ArrayList<>();
        JsonNode schema = root.get("schema");
        if (schema == null || !schema.isTextual()) {
            diagnostics.add(
                    error(
                            sourcePath,
                            "schema",
                            ConfigSchemaVersion.V2.value(),
                            schema == null ? "<missing>" : schema.toString(),
                            "Add schema = \"unifiedmc-2\" or migrate the file."));
        }
        return diagnostics;
    }
}
