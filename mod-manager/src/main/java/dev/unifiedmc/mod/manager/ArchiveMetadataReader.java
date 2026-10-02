package dev.unifiedmc.mod.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import dev.unifiedmc.mod.*;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarFile;

/** Metadata-first reader for Fabric JSON, NeoForge/Forge TOML, and legacy Forge mcmod.info. */
public final class ArchiveMetadataReader implements ModMetadataReader {
    private final ObjectMapper json = new ObjectMapper();
    private final ObjectMapper toml = new TomlMapper();

    @Override
    public boolean supports(List<String> entries) {
        return entries.contains(MetadataFormats.FABRIC)
                || entries.contains(MetadataFormats.NEOFORGE)
                || entries.contains(MetadataFormats.FORGE)
                || entries.contains(MetadataFormats.LEGACY_FORGE);
    }

    public List<ModMetadata> read(ModArtifact artifact, List<String> entries) throws Exception {
        boolean fabric = entries.contains(MetadataFormats.FABRIC);
        boolean neo = entries.contains(MetadataFormats.NEOFORGE);
        boolean forge = entries.contains(MetadataFormats.FORGE);
        boolean legacy = entries.contains(MetadataFormats.LEGACY_FORGE);
        int formats = (fabric ? 1 : 0) + (neo ? 1 : 0) + (forge ? 1 : 0);
        try (JarFile jar = new JarFile(artifact.path().toFile(), false)) {
            if (formats > 1) {
                throw new MetadataFormatException(
                        "Multiple authoritative loader descriptors found");
            }
            if (fabric) {
                return List.of(parseFabric(artifact, readText(jar, MetadataFormats.FABRIC)));
            }
            if (neo) {
                return parseForgeToml(
                        artifact,
                        readText(jar, MetadataFormats.NEOFORGE),
                        ModLoader.NEOFORGE,
                        "neoforge.mods.toml");
            }
            if (forge) {
                return parseForgeToml(
                        artifact,
                        readText(jar, MetadataFormats.FORGE),
                        ModLoader.FORGE,
                        "mods.toml");
            }
            if (legacy) {
                return parseLegacyForge(artifact, readText(jar, MetadataFormats.LEGACY_FORGE));
            }
            return List.of(fallback(artifact));
        }
    }

    public List<String> entries(ModArtifact artifact) throws Exception {
        try (JarFile jar = new JarFile(artifact.path().toFile(), false)) {
            return jar.stream().map(e -> e.getName()).toList();
        }
    }

    private ModMetadata parseFabric(ModArtifact artifact, String text) throws Exception {
        JsonNode root = json.readTree(text);
        if (root.path("schemaVersion").asInt(-1) != 1) {
            throw new MetadataFormatException("Unsupported Fabric fabric.mod.json schemaVersion");
        }
        ModId id = new ModId(required(root, "id"));
        String version = required(root, "version");
        String name = root.path("name").asText(id.value());
        String env = root.path("environment").asText("*");
        List<ModDependency> deps = new ArrayList<>();
        readFabricDeps(root.path("depends"), deps, true);
        Set<ModCapability> caps = new LinkedHashSet<>();
        if (root.path("entrypoints").isObject()) {
            if (root.path("entrypoints").has("main")) {
                caps.add(ModCapability.MAIN_ENTRYPOINT);
            }
            if (root.path("entrypoints").has("server")) {
                caps.add(ModCapability.SERVER_ENTRYPOINT);
            }
            if (root.path("entrypoints").has("client")) {
                caps.add(ModCapability.CLIENT_ENTRYPOINT);
            }
        }
        if (root.path("mixins").isArray()) {
            caps.add(ModCapability.MIXIN);
        }
        if (root.has("accessWidener")) {
            caps.add(ModCapability.ACCESS_WIDENER);
        }
        String mc = dependencyConstraint(root.path("depends"), "minecraft");
        Optional<MinecraftVersion> exact = exactMinecraft(mc);
        return new ModMetadata(
                artifact,
                id,
                version,
                name,
                ModLoader.FABRIC,
                environment(env),
                exact,
                mc,
                dependencyConstraint(root.path("depends"), "fabricloader"),
                deps,
                List.copyOf(caps),
                "fabric.mod.json");
    }

    private void readFabricDeps(JsonNode node, List<ModDependency> out, boolean required) {
        if (!node.isObject()) {
            return;
        }
        node.properties()
                .forEach(
                        entry -> {
                            String id = entry.getKey();
                            JsonNode value = entry.getValue();
                            if (value.isArray()) {
                                String constraint =
                                        java.util.stream.StreamSupport.stream(
                                                        value.spliterator(), false)
                                                .map(JsonNode::asText)
                                                .collect(
                                                        java.util.stream.Collectors.joining(
                                                                " OR "));
                                out.add(
                                        new ModDependency(
                                                id, constraint, required, ModEnvironment.BOTH));
                            } else {
                                out.add(
                                        new ModDependency(
                                                id, value.asText(), required, ModEnvironment.BOTH));
                            }
                        });
    }

    private List<ModMetadata> parseForgeToml(
            ModArtifact artifact, String text, ModLoader loader, String format) throws Exception {
        JsonNode root = toml.readTree(text);
        JsonNode mods = root.path("mods");
        if (!mods.isArray() || mods.isEmpty()) {
            throw new MetadataFormatException(format + " must contain [[mods]] entries");
        }
        List<ModMetadata> result = new ArrayList<>();
        for (JsonNode mod : mods) {
            ModId id = new ModId(required(mod, "modId"));
            String version = mod.path("version").asText("UNKNOWN");
            String name = mod.path("displayName").asText(id.value());
            List<ModDependency> deps = new ArrayList<>();
            JsonNode depGroups = root.path("dependencies").path(id.value());
            if (depGroups.isArray()) {
                for (JsonNode dep : depGroups) {
                    String depId = dep.path("modId").asText("");
                    if (depId.isBlank()) {
                        continue;
                    }
                    deps.add(
                            new ModDependency(
                                    depId,
                                    dep.path("versionRange").asText("*"),
                                    dep.path("mandatory").asBoolean(false),
                                    side(dep.path("side").asText("BOTH"))));
                }
            }
            String mc =
                    deps.stream()
                            .filter(d -> d.modId().equals("minecraft"))
                            .map(ModDependency::versionConstraint)
                            .findFirst()
                            .orElse("");
            String loaderConstraint = root.path("loaderVersion").asText("");
            Optional<MinecraftVersion> exact = exactMavenRange(mc);
            Set<ModCapability> caps = new LinkedHashSet<>();
            if (mod.has("description")) {
                caps.add(ModCapability.RESOURCE_PACK);
            }
            ModEnvironment environment =
                    root.path("clientSideOnly").asBoolean(false)
                            ? ModEnvironment.CLIENT
                            : root.path("serverSideOnly").asBoolean(false)
                                    ? ModEnvironment.SERVER
                                    : ModEnvironment.BOTH;
            result.add(
                    new ModMetadata(
                            artifact,
                            id,
                            version,
                            name,
                            loader,
                            environment,
                            exact,
                            mc,
                            loaderConstraint,
                            deps,
                            List.copyOf(caps),
                            format));
        }
        return List.copyOf(result);
    }

    private List<ModMetadata> parseLegacyForge(ModArtifact artifact, String text) throws Exception {
        JsonNode root = json.readTree(text);
        if (!root.isArray()) {
            throw new MetadataFormatException("mcmod.info must be a JSON array");
        }
        List<ModMetadata> result = new ArrayList<>();
        for (JsonNode mod : root) {
            String rawId = mod.path("modid").asText("");
            if (rawId.isBlank()) {
                continue;
            }
            ModId id = new ModId(rawId);
            String version = mod.path("version").asText("UNKNOWN");
            String mc = mod.path("mcversion").asText("");
            List<ModDependency> deps = new ArrayList<>();
            if (mod.path("requiredMods").isArray()) {
                for (JsonNode d : mod.path("requiredMods")) {
                    deps.add(new ModDependency(d.asText(), "*", true, ModEnvironment.BOTH));
                }
            }
            ModEnvironment environment =
                    mod.path("clientSideOnly").asBoolean(false)
                            ? ModEnvironment.CLIENT
                            : mod.path("serverSideOnly").asBoolean(false)
                                    ? ModEnvironment.SERVER
                                    : ModEnvironment.BOTH;
            result.add(
                    new ModMetadata(
                            artifact,
                            id,
                            version,
                            mod.path("name").asText(id.value()),
                            ModLoader.FORGE,
                            environment,
                            Optional.ofNullable(mc.isBlank() ? null : new MinecraftVersion(mc)),
                            mc,
                            "",
                            deps,
                            List.of(),
                            "mcmod.info"));
        }
        if (result.isEmpty()) {
            throw new MetadataFormatException("mcmod.info contains no valid mod entries");
        }
        return List.copyOf(result);
    }

    private ModMetadata fallback(ModArtifact artifact) {
        String file = artifact.path().getFileName().toString().replaceFirst("(?i)\\.jar$", "");
        String[] parts = file.split("[-_]");
        String id =
                parts.length > 0 && parts[0].matches("[a-z0-9][a-z0-9._-]*") ? parts[0] : "unknown";
        String version = parts.length > 1 ? parts[parts.length - 1] : "UNKNOWN";
        return new ModMetadata(
                artifact,
                new ModId(id),
                version,
                id,
                ModLoader.UNKNOWN,
                ModEnvironment.UNKNOWN,
                Optional.empty(),
                "",
                "",
                List.of(),
                List.of(ModCapability.UNKNOWN),
                "fallback-filename");
    }

    private static ModEnvironment environment(String value) {
        return switch (value) {
            case "client" -> ModEnvironment.CLIENT;
            case "server" -> ModEnvironment.SERVER;
            default -> value.equals("*") ? ModEnvironment.BOTH : ModEnvironment.UNKNOWN;
        };
    }

    private static ModEnvironment side(String value) {
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "CLIENT" -> ModEnvironment.CLIENT;
            case "SERVER" -> ModEnvironment.SERVER;
            default -> ModEnvironment.BOTH;
        };
    }

    private static String required(JsonNode node, String key) throws MetadataFormatException {
        String value = node.path(key).asText("");
        if (value.isBlank()) {
            throw new MetadataFormatException("Missing required metadata property: " + key);
        }
        return value;
    }

    private static String dependencyConstraint(JsonNode deps, String id) {
        JsonNode node = deps.path(id);
        if (node.isArray() && !node.isEmpty()) {
            return node.get(0).asText();
        }
        return node.isTextual() ? node.asText() : "";
    }

    private static Optional<MinecraftVersion> exactMinecraft(String constraint) {
        if (constraint == null
                || constraint.isBlank()
                || constraint.contains(">")
                || constraint.contains("<")
                || constraint.contains("*")) {
            return Optional.empty();
        }
        String value = constraint;
        if (value.startsWith("~")) {
            value = value.substring(1);
        }
        if (value.matches("\\d+\\.\\d+(\\.\\d+)?")) {
            return Optional.of(new MinecraftVersion(value));
        }
        return Optional.empty();
    }

    private static Optional<MinecraftVersion> exactMavenRange(String range) {
        if (range == null || range.isBlank()) {
            return Optional.empty();
        }
        if (range.matches("\\[\\d+\\.\\d+(\\.\\d+)?\\]")) {
            return Optional.of(new MinecraftVersion(range.substring(1, range.length() - 1)));
        }
        return Optional.empty();
    }

    private static String readText(JarFile jar, String name) throws Exception {
        try (InputStream in = jar.getInputStream(jar.getJarEntry(name))) {
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    public static final class MetadataFormatException extends Exception {
        private static final long serialVersionUID = 1L;

        public MetadataFormatException(String message) {
            super(message);
        }
    }
}
