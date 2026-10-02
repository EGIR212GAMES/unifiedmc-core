package dev.unifiedmc.content;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Result returned by a backend compiler before the pipeline persists artifacts. */
public record ContentCompileResult(
        ContentCompilationStatus status,
        String backendId,
        List<ContentDiagnostic> diagnostics,
        List<EmittedArtifact> artifacts,
        List<Registration> registrations) {
    public ContentCompileResult {
        diagnostics = List.copyOf(diagnostics);
        artifacts = List.copyOf(artifacts);
        registrations = List.copyOf(registrations);
    }

    public record EmittedArtifact(Path relativePath, byte[] content) {
        public EmittedArtifact {
            content = content.clone();
        }

        @Override
        public byte[] content() {
            return content.clone();
        }
    }

    public record Registration(String id, String kind, Map<String, String> properties) {
        public Registration {
            properties = Map.copyOf(properties);
        }
    }
}
