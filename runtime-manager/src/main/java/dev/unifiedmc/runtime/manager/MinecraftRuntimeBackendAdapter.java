package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.BackendDescriptor;
import dev.unifiedmc.runtime.BackendId;
import dev.unifiedmc.runtime.BackendLaunchRequest;
import dev.unifiedmc.runtime.BackendState;
import dev.unifiedmc.runtime.JavaRuntimeDescriptor;
import dev.unifiedmc.runtime.MinecraftRuntime;
import dev.unifiedmc.runtime.RuntimeBackend;
import dev.unifiedmc.runtime.RuntimeCapabilities;
import dev.unifiedmc.runtime.RuntimeHandle;
import dev.unifiedmc.runtime.RuntimeHealth;
import dev.unifiedmc.runtime.RuntimeHealthStatus;
import dev.unifiedmc.runtime.RuntimeMetadata;
import dev.unifiedmc.runtime.RuntimeRequest;
import dev.unifiedmc.runtime.UnifiedBackend;
import java.time.Duration;
import java.util.Objects;

/**
 * Transitional adapter allowing the new runtime abstraction to participate in the existing
 * lifecycle SPI.
 */
public final class MinecraftRuntimeBackendAdapter implements UnifiedBackend {
    private final MinecraftRuntime runtime;
    private volatile RuntimeHandle handle;

    public MinecraftRuntimeBackendAdapter(MinecraftRuntime runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    @Override
    public BackendDescriptor descriptor() {
        RuntimeMetadata metadata = runtime.metadata();
        return new BackendDescriptor(
                new BackendId(metadata.runtimeId()),
                metadata.gameVersion(),
                loaderName(metadata.backend()),
                metadata.loaderVersion(),
                metadata.javaRequirement());
    }

    @Override
    public RuntimeCapabilities capabilities() {
        return runtime.capabilities();
    }

    @Override
    public BackendState state() {
        RuntimeHealthStatus status = runtime.health().status();
        return switch (status) {
            case STARTING -> BackendState.STARTING;
            case HEALTHY -> BackendState.RUNNING;
            case STOPPING -> BackendState.STOPPING;
            case FAILED -> BackendState.FAILED;
            case STOPPED, DISCOVERING, INSTALLING, UNKNOWN -> BackendState.STOPPED;
        };
    }

    @Override
    public RuntimeHealth health() {
        return runtime.health();
    }

    @Override
    public synchronized void start(
            JavaRuntimeDescriptor javaRuntime, BackendLaunchRequest request) {
        if (handle != null && handle.process().isAlive()) {
            throw new IllegalStateException(
                    "Backend is already running: " + descriptor().id().value());
        }
        handle =
                runtime.start(
                        new RuntimeRequest(
                                request.workingDirectory(),
                                javaRuntime,
                                request.jvmArguments(),
                                request.applicationArguments(),
                                request.environment(),
                                Duration.ofSeconds(30),
                                Duration.ofSeconds(15)));
    }

    @Override
    public synchronized void stop() {
        if (handle == null) {
            return;
        }
        handle.stop(Duration.ofSeconds(15));
    }

    public MinecraftRuntime runtime() {
        return runtime;
    }

    private static String loaderName(RuntimeBackend backend) {
        return switch (backend) {
            case MODERN_NEOFORGE -> "neoforge";
            case FABRIC_VIA_CONNECTOR -> "fabric-via-connector";
            case LEGACY_FORGE -> "forge";
            case FUTURE_CUSTOM -> "custom";
        };
    }
}
