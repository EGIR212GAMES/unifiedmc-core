package dev.unifiedmc.content.polymer;

import dev.unifiedmc.content.ir.ContentDefinition;
import dev.unifiedmc.content.ir.definitions.ContentBehaviorDefinition;
import dev.unifiedmc.content.ir.definitions.ContentBlockDefinition;
import dev.unifiedmc.content.ir.definitions.ContentEntityDefinition;
import dev.unifiedmc.content.ir.definitions.ContentItemDefinition;
import dev.unifiedmc.content.ir.definitions.ContentModelDefinition;
import dev.unifiedmc.content.ir.definitions.ContentRecipeDefinition;
import dev.unifiedmc.content.ir.definitions.ContentTextureDefinition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Minimal deterministic JSON serializer for generated artifacts; keys are emitted in fixed order.
 */
final class DeterministicJson {
    private DeterministicJson() {}

    static String manifest(
            String adapterId,
            String modId,
            String gameVersion,
            String loader,
            String status,
            List<String> diagnostics) {
        StringBuilder out = new StringBuilder(256);
        out.append("{\n");
        field(out, 1, "adapterId", adapterId, true);
        field(out, 1, "modId", modId, true);
        field(out, 1, "minecraftVersion", gameVersion, true);
        field(out, 1, "loader", loader, true);
        field(out, 1, "status", status, true);
        out.append(indent(1)).append("\"diagnostics\":[");
        for (int i = 0; i < diagnostics.size(); i++) {
            if (i > 0) {

                out.append(',');
            }
            out.append('"').append(escape(diagnostics.get(i))).append('"');
        }
        out.append("]\n");
        out.append("}\n");
        return out.toString();
    }

    static String definition(ContentDefinition definition) {
        StringBuilder out = new StringBuilder(512);
        out.append("{\n");
        field(out, 1, "kind", definition.kind().name().toLowerCase(), true);
        field(out, 1, "id", definition.id().value(), true);
        if (definition instanceof ContentBlockDefinition block) {
            field(out, 1, "hardness", Float.toString(block.hardness()), true);
        } else if (definition instanceof ContentItemDefinition item) {
            field(out, 1, "maxStackSize", Integer.toString(item.maxStackSize()), true);
        } else if (definition instanceof ContentEntityDefinition entity) {
            field(out, 1, "entityKind", entity.entityKind(), true);
        } else if (definition instanceof ContentRecipeDefinition recipe) {
            field(out, 1, "recipeType", recipe.recipeType(), true);
            out.append(indent(1)).append("\"ingredients\":[\n");
            for (int i = 0; i < recipe.ingredients().size(); i++) {
                var ingredient = recipe.ingredients().get(i);
                out.append(indent(2))
                        .append("{\"item\":\"")
                        .append(escape(ingredient.item().value()))
                        .append("\",\"count\":")
                        .append(ingredient.count())
                        .append("}");
                if (i + 1 < recipe.ingredients().size()) {

                    out.append(',');
                }
                out.append('\n');
            }
            out.append(indent(1)).append("],\n");
            out.append(indent(1))
                    .append("\"result\":{\"item\":\"")
                    .append(escape(recipe.result().item().value()))
                    .append("\",\"count\":")
                    .append(recipe.result().count())
                    .append("},\n");
        } else if (definition instanceof ContentModelDefinition model) {
            field(out, 1, "modelType", model.modelType(), true);
            field(out, 1, "source", model.source(), true);
        } else if (definition instanceof ContentTextureDefinition texture) {
            field(out, 1, "assetPath", texture.assetPath(), true);
        } else if (definition instanceof ContentBehaviorDefinition behavior) {
            field(out, 1, "behaviorType", behavior.behaviorType(), true);
        }
        out.append(indent(1)).append("\"properties\":");
        map(out, definition.properties(), 1);
        out.append('\n').append("}\n");
        return out.toString();
    }

    private static void field(
            StringBuilder out, int level, String key, String value, boolean comma) {
        out.append(indent(level))
                .append('"')
                .append(escape(key))
                .append("\":\"")
                .append(escape(value))
                .append('"');
        if (comma) {

            out.append(',');
        }
        out.append('\n');
    }

    private static void map(StringBuilder out, Map<String, String> values, int level) {
        out.append('{');
        List<Map.Entry<String, String>> entries = new ArrayList<>(values.entrySet());
        entries.sort(Comparator.comparing(Map.Entry::getKey));
        if (!entries.isEmpty()) {

            out.append('\n');
        }
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, String> entry = entries.get(i);
            out.append(indent(level + 1))
                    .append('"')
                    .append(escape(entry.getKey()))
                    .append("\":\"")
                    .append(escape(entry.getValue()))
                    .append('"');
            if (i + 1 < entries.size()) {

                out.append(',');
            }
            out.append('\n');
        }
        if (!entries.isEmpty()) {

            out.append(indent(level));
        }
        out.append('}');
    }

    private static String indent(int level) {
        return "  ".repeat(level);
    }

    private static String escape(String value) {
        StringBuilder out = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}
