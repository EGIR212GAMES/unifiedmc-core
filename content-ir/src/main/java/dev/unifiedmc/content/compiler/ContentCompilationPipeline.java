package dev.unifiedmc.content.compiler;

import dev.unifiedmc.content.ContentCompilationContext;
import dev.unifiedmc.content.ContentCompilationStatus;
import dev.unifiedmc.content.ContentCompileResult;
import dev.unifiedmc.content.ContentDiagnostic;
import dev.unifiedmc.content.ir.ContentIrDocument;
import dev.unifiedmc.uapi.adapter.ModAdapter;
import dev.unifiedmc.uapi.compat.CompatibilityReport;
import dev.unifiedmc.uapi.content.UniversalContentBundle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Deterministic six-stage content compiler. Backends receive only normalized Content IR. */
public final class ContentCompilationPipeline {
    private final ContentIrNormalizer normalizer;
    private final ContentIrValidator validator;

    public ContentCompilationPipeline() {
        this(new ContentIrNormalizer(), new ContentIrValidator());
    }

    public ContentCompilationPipeline(
            ContentIrNormalizer normalizer, ContentIrValidator validator) {
        this.normalizer = normalizer;
        this.validator = validator;
    }

    public ContentPipelineResult compile(
            ModAdapter adapter, ContentCompilationContext context, ContentBackendCompiler backend) {
        List<ContentPipelineResult.StageEvent> stages = new ArrayList<>();
        List<ContentDiagnostic> diagnostics = new ArrayList<>();
        ContentIrDocument ir = null;

        CompatibilityReport report = adapter.compatibility(context.compatibilityContext());
        stages.add(
                new ContentPipelineResult.StageEvent(
                        ContentCompilerStage.ANALYZE,
                        ContentPipelineResult.StageStatus.COMPLETED,
                        report.status().name()
                                + (report.unsupportedFeatures().isEmpty()
                                        ? ""
                                        : ": " + String.join("; ", report.unsupportedFeatures()))));
        if (report.status() == CompatibilityReport.Status.PARTIAL) {
            diagnostics.add(
                    new ContentDiagnostic(
                            ContentDiagnostic.Severity.WARNING,
                            "compatibility",
                            "Adapter compatibility is partial",
                            "SUPPORTED",
                            report.unsupportedFeatures().toString(),
                            "Review unsupported features before enabling this content."));
        } else if (report.status() == CompatibilityReport.Status.UNSUPPORTED) {
            diagnostics.add(
                    new ContentDiagnostic(
                            ContentDiagnostic.Severity.ERROR,
                            "compatibility",
                            "Adapter is not compatible with the selected target",
                            "supported target",
                            report.unsupportedFeatures().toString(),
                            "Select a compatible adapter/runtime or add an explicit adapter."));
            return finishUnsupported(adapter, backend, stages, diagnostics, null);
        }

        UniversalContentBundle bundle = adapter.content(context.compatibilityContext());
        for (String feature : normalizer.unsupportedSourceFeatures(bundle)) {
            diagnostics.add(
                    new ContentDiagnostic(
                            ContentDiagnostic.Severity.WARNING,
                            "source." + feature.toLowerCase(),
                            "Content type is present in UAPI but has no Content IR representation in this milestone",
                            "supported Content IR type",
                            feature,
                            "Add a dedicated Content IR definition and backend compiler before enabling this content."));
        }
        ir =
                normalizer.normalize(
                        adapter.adapterId(),
                        adapter.modId(),
                        bundle,
                        context.compatibilityContext());
        stages.add(
                new ContentPipelineResult.StageEvent(
                        ContentCompilerStage.NORMALIZE,
                        ContentPipelineResult.StageStatus.COMPLETED,
                        "Normalized " + ir.definitions().size() + " definitions"));

        List<ContentDiagnostic> validation = validator.validate(ir);
        diagnostics.addAll(validation);
        if (validation.stream().anyMatch(d -> d.severity() == ContentDiagnostic.Severity.ERROR)) {
            stages.add(
                    new ContentPipelineResult.StageEvent(
                            ContentCompilerStage.VALIDATE,
                            ContentPipelineResult.StageStatus.FAILED,
                            "Content IR validation failed"));
            ContentCompileResult result =
                    new ContentCompileResult(
                            ContentCompilationStatus.UNSUPPORTED,
                            backend.backendId(),
                            diagnostics,
                            List.of(),
                            List.of());
            return new ContentPipelineResult(result, ir, stages, diagnostics);
        }
        stages.add(
                new ContentPipelineResult.StageEvent(
                        ContentCompilerStage.VALIDATE,
                        ContentPipelineResult.StageStatus.COMPLETED,
                        "Content IR is valid"));

        ContentCompileResult compiled = backend.compile(ir, context);
        stages.add(
                new ContentPipelineResult.StageEvent(
                        ContentCompilerStage.COMPILE,
                        ContentPipelineResult.StageStatus.COMPLETED,
                        "Backend compilation returned " + compiled.status()));
        diagnostics.addAll(compiled.diagnostics());

        if (!compiled.artifacts().isEmpty()) {
            try {
                emit(context.outputDirectory(), compiled.artifacts());
                stages.add(
                        new ContentPipelineResult.StageEvent(
                                ContentCompilerStage.EMIT,
                                ContentPipelineResult.StageStatus.COMPLETED,
                                "Emitted " + compiled.artifacts().size() + " artifacts"));
            } catch (Exception e) {
                diagnostics.add(
                        new ContentDiagnostic(
                                ContentDiagnostic.Severity.ERROR,
                                context.outputDirectory().toString(),
                                "Failed to emit artifacts",
                                "writable directory",
                                e.getClass().getSimpleName(),
                                "Fix the output directory permissions and retry."));
                stages.add(
                        new ContentPipelineResult.StageEvent(
                                ContentCompilerStage.EMIT,
                                ContentPipelineResult.StageStatus.FAILED,
                                e.getMessage() == null
                                        ? e.getClass().getSimpleName()
                                        : e.getMessage()));
                ContentCompileResult failed =
                        new ContentCompileResult(
                                ContentCompilationStatus.UNSUPPORTED,
                                compiled.backendId(),
                                diagnostics,
                                List.of(),
                                compiled.registrations());
                return new ContentPipelineResult(failed, ir, stages, diagnostics);
            }
        } else {
            stages.add(
                    new ContentPipelineResult.StageEvent(
                            ContentCompilerStage.EMIT,
                            ContentPipelineResult.StageStatus.SKIPPED,
                            "Backend emitted no artifacts"));
        }

        stages.add(
                new ContentPipelineResult.StageEvent(
                        ContentCompilerStage.REGISTER,
                        ContentPipelineResult.StageStatus.COMPLETED,
                        "Recorded " + compiled.registrations().size() + " registration entries"));
        ContentCompilationStatus finalStatus = compiled.status();
        if (!normalizer.unsupportedSourceFeatures(bundle).isEmpty()
                && finalStatus == ContentCompilationStatus.SUPPORTED) {
            finalStatus = ContentCompilationStatus.PARTIAL;
        }
        if (report.status() == CompatibilityReport.Status.PARTIAL
                && finalStatus == ContentCompilationStatus.SUPPORTED) {
            finalStatus = ContentCompilationStatus.PARTIAL;
        }
        if (finalStatus != compiled.status()) {
            compiled =
                    new ContentCompileResult(
                            finalStatus,
                            compiled.backendId(),
                            diagnostics,
                            compiled.artifacts(),
                            compiled.registrations());
        }
        return new ContentPipelineResult(compiled, ir, stages, diagnostics);
    }

    private static ContentPipelineResult finishUnsupported(
            ModAdapter adapter,
            ContentBackendCompiler backend,
            List<ContentPipelineResult.StageEvent> stages,
            List<ContentDiagnostic> diagnostics,
            ContentIrDocument ir) {
        ContentCompileResult result =
                new ContentCompileResult(
                        ContentCompilationStatus.UNSUPPORTED,
                        backend.backendId(),
                        diagnostics,
                        List.of(),
                        List.of());
        return new ContentPipelineResult(result, ir, stages, diagnostics);
    }

    private static void emit(Path root, List<ContentCompileResult.EmittedArtifact> artifacts)
            throws Exception {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        for (var artifact : artifacts) {
            Path relative = artifact.relativePath().normalize();
            if (relative.isAbsolute() || relative.startsWith("..")) {
                throw new IllegalArgumentException(
                        "Artifact path must stay within output directory: " + relative);
            }
            Path path = normalizedRoot.resolve(relative).normalize();
            if (!path.startsWith(normalizedRoot)) {
                throw new IllegalArgumentException(
                        "Artifact escaped output directory: " + relative);
            }
            Files.createDirectories(path.getParent());
            Files.write(path, artifact.content());
        }
    }
}
