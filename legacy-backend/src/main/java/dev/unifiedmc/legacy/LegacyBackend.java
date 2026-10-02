package dev.unifiedmc.legacy;

import dev.unifiedmc.runtime.BackendDescriptor;
import dev.unifiedmc.runtime.BackendLaunchRequest;
import dev.unifiedmc.runtime.BackendState;
import dev.unifiedmc.runtime.JavaRuntimeDescriptor;
import dev.unifiedmc.runtime.RuntimeCapabilities;
import dev.unifiedmc.runtime.RuntimeHealth;
import dev.unifiedmc.runtime.RuntimeRequest;
import dev.unifiedmc.runtime.UnifiedBackend;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;

/** Unified backend facade for one isolated Forge legacy runtime. */
public final class LegacyBackend implements UnifiedBackend {
    private final BackendDescriptor descriptor;
    private final RuntimeCapabilities capabilities;
    private final LegacyRuntime runtime;
    private final LegacyRecoveryPolicy recoveryPolicy;
    private final LegacyBridge bridge;
    private volatile LegacyRuntimeSupervisor supervisor;

    public LegacyBackend(
            BackendDescriptor descriptor,
            LegacyRuntime runtime,
            LegacyRecoveryPolicy recoveryPolicy,
            LegacyBridge bridge) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.recoveryPolicy = Objects.requireNonNull(recoveryPolicy, "recoveryPolicy");
        this.bridge = Objects.requireNonNull(bridge, "bridge");
        this.capabilities =
                new RuntimeCapabilities(
                        Set.of(
                                "isolated-world",
                                "shared-player-bridge",
                                "graceful-shutdown",
                                "crash-recovery",
                                "legacy-forge-runtime",
                                "separate-jvm"));
    }

    @Override
    public BackendDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public RuntimeCapabilities capabilities() {
        return capabilities;
    }

    @Override
    public BackendState state() {
        LegacyRuntimeSupervisor current = supervisor;
        if (current == null) {

            return BackendState.STOPPED;
        }
        return switch (runtime.health().status()) {
            case STARTING, INSTALLING, DISCOVERING -> BackendState.STARTING;
            case HEALTHY -> BackendState.RUNNING;
            case STOPPING -> BackendState.STOPPING;
            case FAILED -> BackendState.FAILED;
            case STOPPED, UNKNOWN -> BackendState.STOPPED;
        };
    }

    @Override
    public RuntimeHealth health() {
        return runtime.health();
    }

    @Override
    public synchronized void start(
            JavaRuntimeDescriptor javaRuntime, BackendLaunchRequest request) {
        if (supervisor != null && state() == BackendState.RUNNING) {
            throw new IllegalStateException(
                    "Legacy backend is already running: " + descriptor.id().value());
        }
        RuntimeRequest runtimeRequest =
                new RuntimeRequest(
                        request.workingDirectory(),
                        javaRuntime,
                        request.jvmArguments(),
                        request.applicationArguments(),
                        request.environment(),
                        Duration.ofSeconds(30),
                        Duration.ofSeconds(15));
        LegacyRuntimeSupervisor newSupervisor =
                new LegacyRuntimeSupervisor(runtime, runtimeRequest, recoveryPolicy, bridge);
        supervisor = newSupervisor;
        newSupervisor.start();
    }

    @Override
    public synchronized void stop() {
        if (supervisor != null) {
            supervisor.stop();
        }
    }

    public LegacyProtocolProfile protocolProfile() {
        return new LegacyProtocolProfile(
                new dev.unifiedmc.version.GameVersion(descriptor.gameVersion()),
                java.util.OptionalInt.empty(),
                java.util.List.of(),
                "ViaVersion/ViaProxy boundary");
    }
}
