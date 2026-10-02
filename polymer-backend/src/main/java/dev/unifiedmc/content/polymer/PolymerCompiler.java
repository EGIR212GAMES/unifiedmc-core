package dev.unifiedmc.content.polymer;

import dev.unifiedmc.content.ContentBackendCapabilities;
import dev.unifiedmc.content.ContentCompilationContext;
import dev.unifiedmc.content.ContentCompilationStatus;
import dev.unifiedmc.content.ContentCompileResult;
import dev.unifiedmc.content.ContentDiagnostic;
import dev.unifiedmc.content.ContentProjectionCapability;
import dev.unifiedmc.content.compiler.ContentBackendCompiler;
import dev.unifiedmc.content.ir.ContentDefinition;
import dev.unifiedmc.content.ir.ContentDefinitionKind;
import dev.unifiedmc.content.ir.ContentIrDocument;
import dev.unifiedmc.uapi.Capability;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Content IR compiler for a future versioned Polymer runtime adapter. */
public final class PolymerCompiler implements ContentBackendCompiler {
    private static final Set<ContentDefinitionKind> SUPPORTED =
            Set.of(
                    ContentDefinitionKind.BLOCK,
                    ContentDefinitionKind.ITEM,
                    ContentDefinitionKind.ENTITY,
                    ContentDefinitionKind.RECIPE,
                    ContentDefinitionKind.MODEL,
                    ContentDefinitionKind.TEXTURE);

    private final PolymerResourceManager resources;
    private final PolymerRegistry registry;
    private final PolymerCapabilityDetector capabilityDetector;

    public PolymerCompiler() {
        this(new PolymerResourceManager(), new PolymerRegistry(), new PolymerCapabilityDetector());
    }

    public PolymerCompiler(
            PolymerResourceManager resources,
            PolymerRegistry registry,
            PolymerCapabilityDetector capabilityDetector) {
        this.resources = resources;
        this.registry = registry;
        this.capabilityDetector = capabilityDetector;
    }

    @Override
    public String backendId() {
        return "polymer";
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
        List<ContentDiagnostic> diagnostics = new ArrayList<>();
        List<ContentDefinition> supported = new ArrayList<>();
        boolean partial = false;

        for (Capability capability : capabilityDetector.unsupported(document.capabilities())) {
            partial = true;
            diagnostics.add(
                    new ContentDiagnostic(
                            ContentDiagnostic.Severity.WARNING,
                            "capability." + capability.name().toLowerCase(),
                            "Requested capability is not representable by the current Polymer backend",
                            capabilityDetector.supported().toString(),
                            capability.name(),
                            "Use a compatible adapter/profile or a backend with explicit support for this capability."));
        }

        for (ContentDefinition definition : document.definitions()) {
            if (SUPPORTED.contains(definition.kind())) {
                supported.add(definition);
            } else {
                partial = true;
                diagnostics.add(
                        new ContentDiagnostic(
                                ContentDiagnostic.Severity.WARNING,
                                definition.stableKey(),
                                "Content definition cannot be represented by the current Polymer backend",
                                SUPPORTED.toString(),
                                definition.kind().name(),
                                "Provide a dedicated backend compiler or an explicit adapter mapping for this content type."));
            }
        }

        List<ContentCompileResult.EmittedArtifact> artifacts = new ArrayList<>();
        artifacts.add(
                new ContentCompileResult.EmittedArtifact(
                        Path.of("unifiedmc-manifest.json"),
                        DeterministicJson.manifest(
                                        document.adapterId(),
                                        document.modId(),
                                        document.minecraftVersion().value(),
                                        document.loader().name(),
                                        partial
                                                ? ContentCompilationStatus.PARTIAL.name()
                                                : ContentCompilationStatus.SUPPORTED.name(),
                                        diagnostics.stream()
                                                .map(ContentDiagnostic::message)
                                                .toList())
                                .getBytes(StandardCharsets.UTF_8)));
        artifacts.addAll(resources.emit(supported));

        ContentCompilationStatus status =
                partial ? ContentCompilationStatus.PARTIAL : ContentCompilationStatus.SUPPORTED;
        return new ContentCompileResult(
                status, backendId(), diagnostics, artifacts, registry.registrations(supported));
    }
}
