package dev.unifiedmc.geyser.compiler;

import dev.unifiedmc.content.ContentCompilationStatus;
import dev.unifiedmc.content.ContentCompileResult;
import dev.unifiedmc.content.ContentDiagnostic;
import dev.unifiedmc.content.ir.ContentIrDocument;
import dev.unifiedmc.content.ir.definitions.ContentBlockDefinition;
import dev.unifiedmc.content.ir.definitions.ContentItemDefinition;
import dev.unifiedmc.geyser.BedrockCapabilityReport;
import dev.unifiedmc.geyser.BedrockDiagnostic;
import dev.unifiedmc.uapi.content.UniversalIdentifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Emits explicit Geyser JSON mapping files; it never claims automatic Java resource-pack
 * conversion.
 */
public final class BedrockMappingCompiler {
    public ContentCompileResult compile(
            ContentIrDocument document, BedrockCapabilityReport report) {
        List<ContentDiagnostic> diagnostics = new ArrayList<>();
        List<ContentCompileResult.EmittedArtifact> artifacts = new ArrayList<>();
        List<ContentCompileResult.Registration> registrations = new ArrayList<>();
        boolean partial = report.isPartial();

        String blockJson = blocksJson(document, diagnostics, registrations);
        if (!blockJson.isEmpty()) {
            artifacts.add(
                    new ContentCompileResult.EmittedArtifact(
                            Path.of("mappings/blocks.json"),
                            blockJson.getBytes(StandardCharsets.UTF_8)));
        }

        String itemJson = itemsJson(document, diagnostics, registrations);
        if (!itemJson.isEmpty()) {
            artifacts.add(
                    new ContentCompileResult.EmittedArtifact(
                            Path.of("mappings/items.json"),
                            itemJson.getBytes(StandardCharsets.UTF_8)));
        }

        if (report.isUnsupported()) {
            partial = true;
        }
        for (BedrockDiagnostic diagnostic : report.diagnostics()) {
            diagnostics.add(
                    new ContentDiagnostic(
                            diagnostic.severity() == BedrockDiagnostic.Severity.ERROR
                                    ? ContentDiagnostic.Severity.ERROR
                                    : diagnostic.severity() == BedrockDiagnostic.Severity.WARNING
                                            ? ContentDiagnostic.Severity.WARNING
                                            : ContentDiagnostic.Severity.INFO,
                            diagnostic.path(),
                            diagnostic.message(),
                            diagnostic.expected(),
                            diagnostic.actual(),
                            diagnostic.fix()));
        }
        ContentCompilationStatus status =
                partial ? ContentCompilationStatus.PARTIAL : ContentCompilationStatus.SUPPORTED;
        if (artifacts.isEmpty() && status == ContentCompilationStatus.SUPPORTED) {
            status = ContentCompilationStatus.PARTIAL;
        }
        return new ContentCompileResult(
                status, "geyser-bedrock", diagnostics, artifacts, registrations);
    }

    private String blocksJson(
            ContentIrDocument document,
            List<ContentDiagnostic> diagnostics,
            List<ContentCompileResult.Registration> registrations) {
        List<ContentBlockDefinition> blocks =
                document.blocks().stream()
                        .sorted(Comparator.comparing(d -> d.id().value()))
                        .toList();
        if (blocks.isEmpty()) {

            return "";
        }
        StringBuilder out = new StringBuilder();
        out.append("{\n  \"format_version\":1,\n  \"blocks\":{");
        for (int i = 0; i < blocks.size(); i++) {
            ContentBlockDefinition block = blocks.get(i);
            if (i > 0) {

                out.append(',');
            }
            String id = block.id().value();
            String name = bedrockName(block.id());
            String texture = block.properties().getOrDefault("texture", name);
            out.append("\n    \"").append(escape(id)).append("\":{");
            out.append("\"name\":\"").append(escape(name)).append("\",");
            out.append("\"material_instances\":{\"*\":{\"texture\":\"")
                    .append(escape(texture))
                    .append("\"}}");
            out.append("}");
            registrations.add(
                    new ContentCompileResult.Registration(
                            id,
                            "geyser-custom-block",
                            Map.of("bedrock_name", name, "texture", texture)));
        }
        out.append("\n  }\n}\n");
        diagnostics.add(
                new ContentDiagnostic(
                        ContentDiagnostic.Severity.INFO,
                        "mappings.blocks",
                        "Generated explicit Geyser custom block mappings",
                        "Geyser custom_mappings block schema",
                        Integer.toString(blocks.size()),
                        "Place the generated file in Geyser custom_mappings and enable custom content."));
        return out.toString();
    }

    private String itemsJson(
            ContentIrDocument document,
            List<ContentDiagnostic> diagnostics,
            List<ContentCompileResult.Registration> registrations) {
        List<ContentItemDefinition> items =
                document.items().stream()
                        .sorted(Comparator.comparing(d -> d.id().value()))
                        .toList();
        if (items.isEmpty()) {

            return "";
        }
        StringBuilder out = new StringBuilder();
        out.append("{\n  \"format_version\":2,\n  \"items\":{");
        boolean emitted = false;
        for (ContentItemDefinition item : items) {
            String base = item.properties().get("bedrock-base-item");
            if (base == null || !base.startsWith("minecraft:")) {

                continue;
            }
            if (emitted) {

                out.append(',');
            }
            emitted = true;
            String id = item.id().value();
            String model = item.properties().getOrDefault("bedrock-model", id);
            out.append("\n    \"")
                    .append(escape(base))
                    .append("\":[{\"type\":\"definition\",\"model\":\"")
                    .append(escape(model))
                    .append("\",\"bedrock_identifier\":\"")
                    .append(escape(id))
                    .append("\"}]");
            registrations.add(
                    new ContentCompileResult.Registration(
                            id, "geyser-custom-item", Map.of("java_base", base, "model", model)));
        }
        out.append("\n  }\n}\n");
        if (!emitted) {

            return "";
        }
        diagnostics.add(
                new ContentDiagnostic(
                        ContentDiagnostic.Severity.INFO,
                        "mappings.items",
                        "Generated explicit Geyser custom item mappings for vanilla-based items",
                        "Geyser custom item JSON v2",
                        Integer.toString(registrations.size()),
                        "Non-vanilla Java items require a Geyser API/extension registration path."));
        return out.toString();
    }

    private static String bedrockName(UniversalIdentifier id) {
        return id.namespace() + "_" + id.path().replace('/', '_');
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
