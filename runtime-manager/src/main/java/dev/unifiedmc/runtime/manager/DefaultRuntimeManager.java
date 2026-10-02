package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.BackendDescriptor;
import dev.unifiedmc.runtime.BackendId;
import dev.unifiedmc.runtime.BackendLaunchRequest;
import dev.unifiedmc.runtime.BackendState;
import dev.unifiedmc.runtime.JavaRuntimeDescriptor;
import dev.unifiedmc.runtime.JavaRuntimeManager;
import dev.unifiedmc.runtime.UnifiedBackend;
import dev.unifiedmc.runtime.manager.neoforge.NeoForgeRuntimeProvider;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Runtime registry and explicit Java-runtime selection boundary. */
public final class DefaultRuntimeManager implements RuntimeManager {
    private final Map<BackendId, UnifiedBackend> backends = new ConcurrentHashMap<>();
    private final JavaRuntimeManager javaRuntimeManager;
    private final NeoForgeRuntimeProvider neoforgeProvider;

    public DefaultRuntimeManager() {
        this(new DefaultJavaRuntimeManager(), new NeoForgeRuntimeProvider());
    }

    public DefaultRuntimeManager(JavaRuntimeManager javaRuntimeManager) {
        this(javaRuntimeManager, new NeoForgeRuntimeProvider());
    }

    public DefaultRuntimeManager(
            JavaRuntimeManager javaRuntimeManager, NeoForgeRuntimeProvider neoforgeProvider) {
        this.javaRuntimeManager = Objects.requireNonNull(javaRuntimeManager, "javaRuntimeManager");
        this.neoforgeProvider = Objects.requireNonNull(neoforgeProvider, "neoforgeProvider");
    }

    @Override
    public void register(UnifiedBackend backend) {
        Objects.requireNonNull(backend, "backend");
        UnifiedBackend previous = backends.putIfAbsent(backend.descriptor().id(), backend);
        if (previous != null) {
            throw new IllegalArgumentException(
                    "Backend already registered: " + backend.descriptor().id().value());
        }
    }

    @Override
    public Optional<UnifiedBackend> find(BackendId id) {
        return Optional.ofNullable(backends.get(Objects.requireNonNull(id, "id")));
    }

    @Override
    public java.util.List<BackendDescriptor> list() {
        return backends.values().stream()
                .map(UnifiedBackend::descriptor)
                .sorted(Comparator.comparing(descriptor -> descriptor.id().value()))
                .toList();
    }

    @Override
    public void start(BackendId id, BackendLaunchRequest request) {
        UnifiedBackend backend =
                find(id).orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Unknown backend: " + id.value()));
        JavaRuntimeDescriptor runtime =
                javaRuntimeManager
                        .select(backend.descriptor().javaRuntime())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No verified Java runtime satisfies Minecraft "
                                                        + backend.descriptor()
                                                                .javaRuntime()
                                                                .minecraftVersion()
                                                                .value()
                                                        + " (minimum Java "
                                                        + backend.descriptor()
                                                                .javaRuntime()
                                                                .minimumJava()
                                                        + ")"));
        backend.start(runtime, request);
    }

    @Override
    public void stop(BackendId id) {
        UnifiedBackend backend =
                find(id).orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Unknown backend: " + id.value()));
        backend.stop();
    }

    @Override
    public BackendState state(BackendId id) {
        return find(id).map(UnifiedBackend::state).orElse(BackendState.STOPPED);
    }

    @Override
    public JavaRuntimeManager javaRuntimes() {
        return javaRuntimeManager;
    }

    @Override
    public void discoverAndRegister(Path runtimeRoot) {
        // Discover NeoForge backends
        for (BackendDescriptor descriptor : neoforgeProvider.listBackends(runtimeRoot)) {
            if (!backends.containsKey(descriptor.id())) {
                neoforgeProvider
                        .createBackend(descriptor.id(), runtimeRoot)
                        .ifPresent(this::register);
            }
        }
    }
}
