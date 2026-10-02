package dev.unifiedmc.geyser.compiler;

import dev.unifiedmc.content.ContentBackendCapabilities;
import dev.unifiedmc.content.ContentCompilationContext;
import dev.unifiedmc.content.ContentCompilationStatus;
import dev.unifiedmc.content.ContentCompileResult;
import dev.unifiedmc.content.ContentDiagnostic;
import dev.unifiedmc.content.ContentProjectionCapability;
import dev.unifiedmc.content.compiler.ContentBackendCompiler;
import dev.unifiedmc.content.ir.ContentIrDocument;
import dev.unifiedmc.geyser.BedrockCompilationResult;
import dev.unifiedmc.geyser.BedrockDiagnostic;
import dev.unifiedmc.geyser.pack.BedrockResourcePackBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/** Coordinates capability analysis, mapping compilation, resource-pack emission and diagnostics. */
public final class BedrockCompiler implements ContentBackendCompiler {
    private final BedrockCapabilityAnalyzer analyzer;
    private final BedrockMappingCompiler mappings;
    private final BedrockResourcePackBuilder packs;

    public BedrockCompiler() {
        this(
                new BedrockCapabilityAnalyzer(),
                new BedrockMappingCompiler(),
                new BedrockResourcePackBuilder());
    }

    public BedrockCompiler(
            BedrockCapabilityAnalyzer analyzer,
            BedrockMappingCompiler mappings,
            BedrockResourcePackBuilder packs) {
        this.analyzer = analyzer;
        this.mappings = mappings;
        this.packs = packs;
    }

    @Override
    public String backendId() {
        return "geyser-bedrock";
    }

    @Override
    public ContentBackendCapabilities capabilities() {
        return new ContentBackendCapabilities(
                EnumSet.of(
                        ContentProjectionCapability.BLOCKS,
                        ContentProjectionCapability.ITEMS,
                        ContentProjectionCapability.ENTITIES,
                        ContentProjectionCapability.RECIPES,
                        ContentProjectionCapability.RESOURCE_PACKS));
    }

    @Override
    public ContentCompileResult compile(
            ContentIrDocument document, ContentCompilationContext context) {
        return compileDetailed(document, context).compileResult();
    }

    public BedrockCompilationResult compileDetailed(
            ContentIrDocument document, ContentCompilationContext context) {
        var report = analyzer.analyze(document);
        ContentCompileResult mappingsResult = mappings.compile(document, report);
        List<ContentCompileResult.EmittedArtifact> artifacts =
                new ArrayList<>(mappingsResult.artifacts());
        artifacts.addAll(packs.build(document));

        List<ContentDiagnostic> diagnostics = new ArrayList<>(mappingsResult.diagnostics());
        diagnostics.add(
                new ContentDiagnostic(
                        ContentDiagnostic.Severity.WARNING,
                        "resourcepack.assets",
                        "Generated pack does not contain source textures because Java resource packs are not converted automatically",
                        "Bedrock-native asset bytes supplied by an asset provider",
                        "no source asset provider",
                        "Provide Bedrock-native textures/geometry through a dedicated adapter asset provider."));

        if (report.status(dev.unifiedmc.geyser.BedrockCapability.ENTITY_REGISTRATION)
                == dev.unifiedmc.geyser.BedrockCapabilityStatus.UNSUPPORTED) {
            diagnostics.add(
                    new ContentDiagnostic(
                            ContentDiagnostic.Severity.WARNING,
                            "entities.registration",
                            "Entity registration requires a version-pinned Geyser extension/API adapter",
                            "Geyser extension",
                            "not emitted",
                            "Add an explicit Geyser extension adapter instead of inferring entity behavior."));
        }

        Path diagnosticPath = Path.of("diagnostics/report.json");
        artifacts.add(
                new ContentCompileResult.EmittedArtifact(
                        diagnosticPath,
                        diagnosticsJson(document, report, diagnostics)
                                .getBytes(StandardCharsets.UTF_8)));
        artifacts.add(
                new ContentCompileResult.EmittedArtifact(
                        Path.of("manifest/bedrock.json"),
                        manifestJson(document, report).getBytes(StandardCharsets.UTF_8)));

        boolean partial =
                mappingsResult.status() != ContentCompilationStatus.SUPPORTED
                        || report.isPartial()
                        || report.isUnsupported();
        ContentCompilationStatus status =
                partial ? ContentCompilationStatus.PARTIAL : ContentCompilationStatus.SUPPORTED;
        ContentCompileResult result =
                new ContentCompileResult(
                        status,
                        backendId(),
                        diagnostics,
                        artifacts,
                        mappingsResult.registrations());
        List<BedrockDiagnostic> bedrockDiagnostics = new ArrayList<>(report.diagnostics());
        return new BedrockCompilationResult(status, document, report, result, bedrockDiagnostics);
    }

    private static String manifestJson(
            ContentIrDocument document, dev.unifiedmc.geyser.BedrockCapabilityReport report) {
        return "{\n"
                + "  \"schema\":\"unifiedmc-bedrock-1\",\n"
                + "  \"adapterId\":\""
                + esc(document.adapterId())
                + "\",\n"
                + "  \"modId\":\""
                + esc(document.modId())
                + "\",\n"
                + "  \"minecraftVersion\":\""
                + esc(document.minecraftVersion().value())
                + "\",\n"
                + "  \"loader\":\""
                + esc(document.loader().name())
                + "\",\n"
                + "  \"status\":\""
                + (report.isPartial() || report.isUnsupported() ? "PARTIAL" : "SUPPORTED")
                + "\",\n"
                + "  \"geyserCustomContentRequired\":true\n"
                + "}\n";
    }

    private static String diagnosticsJson(
            ContentIrDocument document,
            dev.unifiedmc.geyser.BedrockCapabilityReport report,
            List<ContentDiagnostic> diagnostics) {
        StringBuilder out =
                new StringBuilder("{\n  \"schema\":\"unifiedmc-bedrock-diagnostics-1\",\n");
        out.append("  \"modId\":\"").append(esc(document.modId())).append("\",\n");
        out.append("  \"capabilities\":{");
        var entries =
                report.capabilities().entrySet().stream()
                        .sorted(java.util.Map.Entry.comparingByKey())
                        .toList();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {

                out.append(',');
            }
            out.append("\n    \"")
                    .append(entries.get(i).getKey().name())
                    .append("\":\"")
                    .append(entries.get(i).getValue().name())
                    .append("\"");
        }
        out.append("\n  },\n  \"diagnostics\":[");
        for (int i = 0; i < diagnostics.size(); i++) {
            if (i > 0) {

                out.append(',');
            }
            var d = diagnostics.get(i);
            out.append("\n    {\"severity\":\"")
                    .append(d.severity().name())
                    .append("\",\"path\":\"")
                    .append(esc(d.path()))
                    .append("\",\"message\":\"")
                    .append(esc(d.message()))
                    .append("\"}");
        }
        out.append("\n  ]\n}\n");
        return out.toString();
    }

    private static String esc(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
