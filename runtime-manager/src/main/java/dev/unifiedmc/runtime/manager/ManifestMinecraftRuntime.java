package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.RuntimeInstallationManifest;
import dev.unifiedmc.runtime.RuntimeMetadata;
import dev.unifiedmc.runtime.RuntimeValidator;
import dev.unifiedmc.version.GameVersion;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import java.nio.file.Path;

/** Generic manifest-backed runtime used until a backend-specific provider is installed. */
final class ManifestMinecraftRuntime extends AbstractProcessRuntime {
    ManifestMinecraftRuntime(
            RuntimeInstallationManifest manifest, Path root, RuntimeValidator validator) {
        super(metadata(manifest, root), manifest, validator);
    }

    private static RuntimeMetadata metadata(RuntimeInstallationManifest manifest, Path root) {
        return new RuntimeMetadata(
                manifest.runtimeId(),
                manifest.gameVersion(),
                manifest.backend(),
                manifest.loaderVersion(),
                new JavaRuntimeRequirement(
                        new GameVersion(manifest.gameVersion()),
                        manifest.minimumJava(),
                        manifest.recommendedJava(),
                        "runtime installation manifest"),
                root,
                Path.of(manifest.launchArtifact()),
                manifest.verified(),
                manifest.artifactSha256(),
                manifest.sourceId());
    }
}
