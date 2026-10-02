package dev.unifiedmc.legacy;

import dev.unifiedmc.runtime.RuntimeHandle;
import dev.unifiedmc.runtime.RuntimeRequest;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Supervises one legacy process and keeps Core alive if that process crashes. */
public final class LegacyRuntimeSupervisor implements AutoCloseable {
    private final LegacyRuntime runtime;
    private final RuntimeRequest request;
    private final LegacyRecoveryPolicy recoveryPolicy;
    private final LegacyBridge bridge;
    private final AtomicInteger restarts = new AtomicInteger();
    private final AtomicBoolean closed = new AtomicBoolean();
    private volatile RuntimeHandle handle;
    private volatile Thread monitorThread;

    public LegacyRuntimeSupervisor(
            LegacyRuntime runtime,
            RuntimeRequest request,
            LegacyRecoveryPolicy recoveryPolicy,
            LegacyBridge bridge) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.request = Objects.requireNonNull(request, "request");
        this.recoveryPolicy = Objects.requireNonNull(recoveryPolicy, "recoveryPolicy");
        this.bridge = Objects.requireNonNull(bridge, "bridge");
    }

    public synchronized RuntimeHandle start() {
        if (closed.get()) {
            throw new IllegalStateException("Legacy runtime supervisor is closed");
        }
        if (handle != null && handle.process().isAlive()) {
            throw new IllegalStateException("Legacy runtime is already running");
        }
        bridge.statusChanged(
                runtime.backendId(), LegacyBackendStatus.STARTING, "Launching legacy runtime");
        handle = runtime.start(request);
        restarts.set(0);
        bridge.statusChanged(
                runtime.backendId(), LegacyBackendStatus.RUNNING, "Legacy runtime process started");
        monitorThread =
                new Thread(this::monitor, "unifiedmc-legacy-monitor-" + runtime.backendId());
        monitorThread.setDaemon(true);
        monitorThread.start();
        return handle;
    }

    private void monitor() {
        RuntimeHandle observed = handle;
        if (observed == null) {

            return;
        }
        try {
            while (!closed.get() && observed.process().isAlive()) {
                Thread.sleep(250L);
            }
            if (closed.get()) {

                return;
            }
            if (observed.health().status() == dev.unifiedmc.runtime.RuntimeHealthStatus.FAILED) {
                String diagnostic = observed.health().diagnostic();
                bridge.statusChanged(runtime.backendId(), LegacyBackendStatus.FAILED, diagnostic);
                bridge.notifyPlayerError(
                        new LegacyPlayerError(
                                runtime.backendId(),
                                "LEGACY_BACKEND_FAILED",
                                "The legacy backend became unavailable. Core remains online."));
                recover();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException exception) {
            bridge.statusChanged(
                    runtime.backendId(), LegacyBackendStatus.FAILED, exception.getMessage());
        }
    }

    private synchronized void recover() {
        if (!recoveryPolicy.autoRestart() || closed.get()) {
            return;
        }
        int attempt = restarts.incrementAndGet();
        if (attempt > recoveryPolicy.maxRestarts()) {
            bridge.statusChanged(
                    runtime.backendId(),
                    LegacyBackendStatus.FAILED,
                    "Automatic restart limit reached");
            return;
        }
        try {
            Duration delay = recoveryPolicy.restartDelay();
            if (!delay.isZero()) {

                Thread.sleep(delay.toMillis());
            }
            if (closed.get()) {

                return;
            }
            bridge.statusChanged(
                    runtime.backendId(),
                    LegacyBackendStatus.STARTING,
                    "Automatic restart attempt " + attempt);
            handle = runtime.start(request);
            bridge.statusChanged(
                    runtime.backendId(), LegacyBackendStatus.RUNNING, "Legacy runtime restarted");
            monitorThread =
                    new Thread(this::monitor, "unifiedmc-legacy-monitor-" + runtime.backendId());
            monitorThread.setDaemon(true);
            monitorThread.start();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException exception) {
            bridge.statusChanged(
                    runtime.backendId(),
                    LegacyBackendStatus.FAILED,
                    "Restart failed: " + exception.getMessage());
            bridge.notifyPlayerError(
                    new LegacyPlayerError(
                            runtime.backendId(),
                            "LEGACY_BACKEND_RESTART_FAILED",
                            "The legacy backend could not be restarted automatically."));
        }
    }

    public synchronized void stop() {
        if (closed.compareAndSet(false, true)) {
            RuntimeHandle current = handle;
            if (current != null) {
                bridge.statusChanged(
                        runtime.backendId(),
                        LegacyBackendStatus.STOPPING,
                        "Stopping legacy runtime");
                current.stop();
                bridge.statusChanged(
                        runtime.backendId(), LegacyBackendStatus.STOPPED, "Legacy runtime stopped");
            }
        }
    }

    @Override
    public void close() {
        stop();
    }
}
