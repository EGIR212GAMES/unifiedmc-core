package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.MinecraftRuntime;
import dev.unifiedmc.runtime.RuntimeDiscoveryReport;
import dev.unifiedmc.runtime.RuntimeRegistry;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** File-backed registry for isolated runtime installations. */
public final class DefaultRuntimeRegistry implements RuntimeRegistry {
    private final Path runtimeRoot;
    private final ManifestRuntimeProvider provider;
    private final Map<String, MinecraftRuntime> runtimes = new ConcurrentHashMap<>();
    private volatile List<String> diagnostics = List.of();

    public DefaultRuntimeRegistry(Path runtimeRoot) {
        this(runtimeRoot, new ManifestRuntimeProvider());
    }

    public DefaultRuntimeRegistry(Path runtimeRoot, ManifestRuntimeProvider provider) {
        this.runtimeRoot = Objects.requireNonNull(runtimeRoot, "runtimeRoot");
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    @Override
    public synchronized RuntimeDiscoveryReport discover() {
        RuntimeDiscoveryReport report = provider.discoverReport(runtimeRoot);
        runtimes.clear();
        for (MinecraftRuntime runtime : report.runtimes()) {
            runtimes.put(runtime.metadata().runtimeId(), runtime);
        }
        diagnostics = report.diagnostics();
        return report;
    }

    @Override
    public List<MinecraftRuntime> list() {
        return runtimes.values().stream()
                .sorted(Comparator.comparing(runtime -> runtime.metadata().runtimeId()))
                .toList();
    }

    @Override
    public Optional<MinecraftRuntime> find(String runtimeId) {
        return Optional.ofNullable(runtimes.get(Objects.requireNonNull(runtimeId, "runtimeId")));
    }

    public List<String> diagnostics() {
        return diagnostics;
    }

    public Path runtimeRoot() {
        return runtimeRoot;
    }
}
