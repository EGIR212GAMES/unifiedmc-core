package dev.unifiedmc.geyser.compiler;

import dev.unifiedmc.content.ir.ContentDefinitionKind;
import dev.unifiedmc.content.ir.ContentIrDocument;
import dev.unifiedmc.content.ir.definitions.ContentItemDefinition;
import dev.unifiedmc.geyser.BedrockCapability;
import dev.unifiedmc.geyser.BedrockCapabilityReport;
import dev.unifiedmc.geyser.BedrockCapabilityStatus;
import dev.unifiedmc.geyser.BedrockDiagnostic;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Analyzes whether Content IR can be explicitly projected through current Geyser mechanisms. */
public final class BedrockCapabilityAnalyzer {
    public BedrockCapabilityReport analyze(ContentIrDocument document) {
        Map<BedrockCapability, BedrockCapabilityStatus> statuses =
                new EnumMap<>(BedrockCapability.class);
        List<BedrockDiagnostic> diagnostics = new ArrayList<>();

        statuses.put(BedrockCapability.RESOURCE_PACK, BedrockCapabilityStatus.SUPPORTED);
        statuses.put(BedrockCapability.GEYSER_EXTENSION, BedrockCapabilityStatus.UNSUPPORTED);

        if (!document.blocks().isEmpty()) {
            statuses.put(BedrockCapability.BLOCK_REGISTRATION, BedrockCapabilityStatus.SUPPORTED);
            statuses.put(BedrockCapability.BLOCK_VISUAL, BedrockCapabilityStatus.SUPPORTED);
            boolean blockBehavior =
                    document.definitions().stream()
                            .filter(d -> d.kind() == ContentDefinitionKind.BLOCK)
                            .anyMatch(
                                    d ->
                                            document.behaviors().stream()
                                                    .anyMatch(b -> b.id().equals(d.id())));
            statuses.put(
                    BedrockCapability.BLOCK_INTERACTION,
                    blockBehavior
                            ? BedrockCapabilityStatus.PARTIAL
                            : BedrockCapabilityStatus.PARTIAL);
            diagnostics.add(
                    new BedrockDiagnostic(
                            BedrockDiagnostic.Severity.WARNING,
                            "blocks.interaction",
                            "Block interaction behavior is not inferred automatically",
                            "explicit Geyser custom block components/permutations",
                            "not declared",
                            "Provide an explicit Geyser extension or backend-specific interaction mapping."));
        }

        if (!document.items().isEmpty()) {
            boolean allHaveVanillaBase = document.items().stream().allMatch(this::hasVanillaBase);
            statuses.put(
                    BedrockCapability.ITEM_REGISTRATION,
                    allHaveVanillaBase
                            ? BedrockCapabilityStatus.SUPPORTED
                            : BedrockCapabilityStatus.PARTIAL);
            statuses.put(BedrockCapability.ITEM_VISUAL, BedrockCapabilityStatus.SUPPORTED);
            statuses.put(
                    BedrockCapability.ITEM_INTERACTION,
                    allHaveVanillaBase
                            ? BedrockCapabilityStatus.PARTIAL
                            : BedrockCapabilityStatus.UNSUPPORTED);
            if (!allHaveVanillaBase) {
                diagnostics.add(
                        new BedrockDiagnostic(
                                BedrockDiagnostic.Severity.WARNING,
                                "items.registration",
                                "Non-vanilla Java items require Geyser API/extension registration; JSON mappings alone are insufficient",
                                "vanilla base item or Geyser extension",
                                "modded non-vanilla item",
                                "Implement a version-pinned Geyser extension adapter before enabling full item registration."));
            }
        }

        if (!document.entities().isEmpty()) {
            statuses.put(
                    BedrockCapability.ENTITY_REGISTRATION, BedrockCapabilityStatus.UNSUPPORTED);
            statuses.put(BedrockCapability.ENTITY_VISUAL, BedrockCapabilityStatus.PARTIAL);
            statuses.put(BedrockCapability.ENTITY_BEHAVIOR, BedrockCapabilityStatus.UNSUPPORTED);
            diagnostics.add(
                    new BedrockDiagnostic(
                            BedrockDiagnostic.Severity.WARNING,
                            "entities",
                            "This milestone does not provide a version-pinned Geyser custom entity extension",
                            "Geyser extension/API entity registration",
                            "not implemented",
                            "Add a Geyser extension adapter for entity registration and translation."));
        }

        if (!document.recipes().isEmpty()) {
            statuses.put(BedrockCapability.RECIPE_REGISTRATION, BedrockCapabilityStatus.PARTIAL);
            diagnostics.add(
                    new BedrockDiagnostic(
                            BedrockDiagnostic.Severity.INFO,
                            "recipes",
                            "Recipe identifiers are preserved in diagnostics, but Bedrock recipe registration is not synthesized",
                            "explicit Bedrock recipe/add-on mapping",
                            "generic Java recipe",
                            "Provide a Bedrock recipe compiler before claiming interaction parity."));
        }

        for (var behavior : document.behaviors()) {
            diagnostics.add(
                    new BedrockDiagnostic(
                            BedrockDiagnostic.Severity.WARNING,
                            behavior.id().value(),
                            "Generic content behavior is not automatically translated to Bedrock",
                            "explicit Bedrock behavior/add-on mapping",
                            behavior.behaviorType(),
                            "Use a dedicated adapter or Geyser extension."));
        }

        return new BedrockCapabilityReport(statuses, diagnostics);
    }

    private boolean hasVanillaBase(ContentItemDefinition item) {
        String base = item.properties().get("bedrock-base-item");
        return base != null && base.startsWith("minecraft:");
    }
}
