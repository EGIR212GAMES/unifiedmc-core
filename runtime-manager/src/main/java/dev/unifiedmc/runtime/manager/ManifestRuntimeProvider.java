package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.MinecraftRuntime;
import dev.unifiedmc.runtime.RuntimeBackend;
import dev.unifiedmc.runtime.RuntimeDiscoveryReport;
import dev.unifiedmc.runtime.RuntimeInstallationManifest;
import dev.unifiedmc.runtime.RuntimeProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Generic provider for manifest-backed installations; no loader classes are loaded. */
public final class ManifestRuntimeProvider implements RuntimeProvider {
    private final RuntimeManifestCodec codec = new RuntimeManifestCodec();
    private final RuntimeValidator validator;

    public ManifestRuntimeProvider() {
        this(new RuntimeValidator());
    }

    public ManifestRuntimeProvider(RuntimeValidator validator) {
        this.validator = validator;
    }

    @Override
    public String providerId() {
        return "manifest";
    }

    @Override
    public boolean supports(RuntimeBackend backend) {
        return true;
    }

    public RuntimeDiscoveryReport discoverReport(Path runtimeRoot) {
        List<MinecraftRuntime> runtimes = new ArrayList<>();
        List<String> diagnostics = new ArrayList<>();
        if (!Files.isDirectory(runtimeRoot)) {
            return new RuntimeDiscoveryReport(
                    List.of(), List.of("Runtime registry directory is missing: " + runtimeRoot));
        }
        try (var versions = Files.list(runtimeRoot)) {
            versions.filter(Files::isDirectory)
                    .sorted()
                    .forEach(
                            versionDir -> {
                                try (var backends = Files.list(versionDir)) {
                                    backends.filter(Files::isDirectory)
                                            .sorted()
                                            .forEach(
                                                    backendDir -> {
                                                        Path manifestPath =
                                                                backendDir.resolve(
                                                                        RuntimeManifestCodec
                                                                                .FILE_NAME);
                                                        if (!Files.isRegularFile(manifestPath)) {
                                                            return;
                                                        }
                                                        try {
                                                            RuntimeInstallationManifest manifest =
                                                                    codec.read(manifestPath);
                                                            RuntimeValidationResultHolder holder =
                                                                    validateManifest(
                                                                            manifest,
                                                                            backendDir,
                                                                            diagnostics);
                                                            if (holder.runtime != null) {
                                                                runtimes.add(holder.runtime);
                                                            }
                                                        } catch (IOException
                                                                | RuntimeException exception) {
                                                            diagnostics.add(
                                                                    manifestPath
                                                                            + ": invalid installation manifest: "
                                                                            + exception
                                                                                    .getMessage());
                                                        }
                                                    });
                                } catch (IOException exception) {
                                    diagnostics.add(
                                            versionDir
                                                    + ": cannot scan backend directories: "
                                                    + exception.getMessage());
                                }
                            });
        } catch (IOException exception) {
            diagnostics.add(
                    runtimeRoot + ": cannot scan runtime registry: " + exception.getMessage());
        }
        runtimes.sort(Comparator.comparing(runtime -> runtime.metadata().runtimeId()));
        return new RuntimeDiscoveryReport(runtimes, diagnostics);
    }

    private RuntimeValidationResultHolder validateManifest(
            RuntimeInstallationManifest manifest, Path root, List<String> diagnostics) {
        var validation = validator.validateManifestOnly(root, manifest);
        if (!validation.valid()) {
            diagnostics.add(
                    root.resolve(RuntimeManifestCodec.FILE_NAME)
                            + ": "
                            + String.join("; ", validation.errors()));
        }
        return new RuntimeValidationResultHolder(
                new ManifestMinecraftRuntime(manifest, root, validator));
    }

    @Override
    public List<MinecraftRuntime> discover(Path runtimeRoot) {
        return discoverReport(runtimeRoot).runtimes();
    }

    private record RuntimeValidationResultHolder(MinecraftRuntime runtime) {}
}
