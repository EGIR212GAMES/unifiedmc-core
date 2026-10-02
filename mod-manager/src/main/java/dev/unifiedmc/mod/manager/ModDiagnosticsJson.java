package dev.unifiedmc.mod.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.unifiedmc.mod.ModAnalysis;
import dev.unifiedmc.mod.ModDependency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Renders diagnostics using a stable primitive-oriented JSON schema for GUI/launcher consumers. */
public final class ModDiagnosticsJson {
    private final ObjectMapper mapper = new ObjectMapper();

    public String render(ModDiagnosticReport report) {
        try {
            Map<String, Object> document = new LinkedHashMap<>();
            document.put("schema", report.schema());
            document.put(
                    "analyses",
                    report.analyses().stream().map(ModDiagnosticsJson::analysis).toList());
            Map<String, List<String>> graph = new LinkedHashMap<>();
            report.graph()
                    .edges()
                    .forEach(
                            (id, dependents) ->
                                    graph.put(
                                            id.value(),
                                            dependents.stream()
                                                    .map(value -> value.value())
                                                    .toList()));
            document.put("graph", graph);
            document.put(
                    "diagnostics",
                    report.diagnostics().stream()
                            .map(
                                    d ->
                                            Map.of(
                                                    "code",
                                                    d.code(),
                                                    "severity",
                                                    d.severity(),
                                                    "path",
                                                    d.path(),
                                                    "property",
                                                    d.property(),
                                                    "message",
                                                    d.message(),
                                                    "fix",
                                                    d.fix()))
                            .toList());
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(document);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to render mod diagnostics JSON", exception);
        }
    }

    private static Map<String, Object> analysis(ModAnalysis a) {
        var m = a.metadata();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("artifact", m.artifact().path().toString());
        result.put("sha256", m.artifact().sha256());
        result.put("id", m.id().value());
        result.put("version", m.version());
        result.put("name", m.name());
        result.put("loader", m.loader().name());
        result.put("environment", m.environment().name());
        result.put("minecraftVersion", m.minecraftVersion().map(v -> v.value()).orElse(null));
        result.put("minecraftConstraint", m.minecraftVersionConstraint());
        result.put("metadataFormat", m.metadataFormat());
        result.put(
                "dependencies",
                m.dependencies().stream().map(ModDiagnosticsJson::dependency).toList());
        result.put("capabilities", m.capabilities().stream().map(Enum::name).toList());
        Map<String, Object> compatibility = new LinkedHashMap<>();
        compatibility.put("status", a.compatibility().status().name());
        compatibility.put("runtime", a.compatibility().assignedRuntime().orElse(null));
        compatibility.put("reasons", a.compatibility().reasons());
        compatibility.put("diagnostics", a.compatibility().diagnostics());
        result.put("compatibility", compatibility);
        result.put("pipeline", a.stages().stream().map(Enum::name).toList());
        return result;
    }

    private static Map<String, Object> dependency(ModDependency d) {
        return Map.of(
                "id",
                d.modId(),
                "version",
                d.versionConstraint(),
                "required",
                d.required(),
                "environment",
                d.environment().name());
    }
}
