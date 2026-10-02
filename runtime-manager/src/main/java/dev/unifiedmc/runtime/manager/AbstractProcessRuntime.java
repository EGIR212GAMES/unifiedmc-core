package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.MinecraftRuntime;
import dev.unifiedmc.runtime.RuntimeCapabilities;
import dev.unifiedmc.runtime.RuntimeCrashDiagnostics;
import dev.unifiedmc.runtime.RuntimeHandle;
import dev.unifiedmc.runtime.RuntimeHealth;
import dev.unifiedmc.runtime.RuntimeHealthStatus;
import dev.unifiedmc.runtime.RuntimeInstallationManifest;
import dev.unifiedmc.runtime.RuntimeMetadata;
import dev.unifiedmc.runtime.RuntimeProcess;
import dev.unifiedmc.runtime.RuntimeRequest;
import dev.unifiedmc.runtime.RuntimeValidationResult;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;

/** Generic isolated process runtime. It executes only verified, rooted launch artifacts. */
public abstract class AbstractProcessRuntime implements MinecraftRuntime {
    private final RuntimeMetadata metadata;
    private final RuntimeInstallationManifest manifest;
    private final RuntimeValidator validator;
    private final RuntimeCapabilities capabilities;
    private volatile ManagedRuntimeProcess process;

    protected AbstractProcessRuntime(
            RuntimeMetadata metadata,
            RuntimeInstallationManifest manifest,
            RuntimeValidator validator) {
        this.metadata = metadata;
        this.manifest = manifest;
        this.validator = validator;
        this.capabilities =
                new RuntimeCapabilities(
                        java.util.Set.of(
                                "process-isolation",
                                "stdout-capture",
                                "stderr-capture",
                                "graceful-shutdown",
                                "health-check",
                                "crash-diagnostics",
                                "checksum-verification"));
    }

    @Override
    public final RuntimeMetadata metadata() {
        return metadata;
    }

    @Override
    public final RuntimeCapabilities capabilities() {
        return capabilities;
    }

    @Override
    public final synchronized RuntimeHandle start(RuntimeRequest request) {
        if (process != null && process.isAlive()) {
            throw new IllegalStateException("Runtime is already running: " + metadata.runtimeId());
        }
        if (!Files.isDirectory(request.workingDirectory())) {
            throw new IllegalArgumentException(
                    "Runtime working directory does not exist: " + request.workingDirectory());
        }
        RuntimeValidationResult validation =
                validator.validate(metadata.rootDirectory(), manifest, request.javaRuntime());
        if (!validation.valid()) {
            throw new IllegalStateException(
                    "Runtime validation failed: " + String.join("; ", validation.errors()));
        }
        ProcessBuilder builder = new ProcessBuilder(buildCommand(request));
        builder.directory(request.workingDirectory().toFile());
        builder.environment().putAll(request.environment());
        try {
            Process started = builder.start();
            ManagedRuntimeProcess managed = new ManagedRuntimeProcess(started);
            process = managed;
            managed.startCapture();
            return new ManagedRuntimeHandle(managed, request.shutdownTimeout());
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to launch runtime "
                            + metadata.runtimeId()
                            + ": "
                            + exception.getMessage(),
                    exception);
        }
    }

    @Override
    public final RuntimeHealth health() {
        ManagedRuntimeProcess current = process;
        if (current == null) {
            return new RuntimeHealth(
                    RuntimeHealthStatus.STOPPED,
                    Instant.now(),
                    -1,
                    "Runtime is not running",
                    Optional.empty());
        }
        return current.health();
    }

    protected List<String> buildCommand(RuntimeRequest request) {
        List<String> command = new java.util.ArrayList<>();
        command.add(request.javaRuntime().javaExecutable().toString());
        command.addAll(request.jvmArguments());
        command.addAll(manifest.launchArguments());
        command.addAll(request.applicationArguments());
        return List.copyOf(command);
    }

    protected final RuntimeInstallationManifest manifest() {
        return manifest;
    }

    private static final class ManagedRuntimeHandle implements RuntimeHandle {
        private final ManagedRuntimeProcess process;
        private final Duration shutdownTimeout;

        private ManagedRuntimeHandle(ManagedRuntimeProcess process, Duration shutdownTimeout) {
            this.process = process;
            this.shutdownTimeout = shutdownTimeout;
        }

        @Override
        public RuntimeProcess process() {
            return process;
        }

        @Override
        public RuntimeHealth health() {
            return process.health();
        }

        @Override
        public void stop() {
            stop(shutdownTimeout);
        }

        @Override
        public void stop(Duration timeout) {
            if (!process.isAlive()) {

                return;
            }
            process.stopGracefully(timeout);
        }

        @Override
        public void destroyForcibly() {
            process.destroyForcibly();
        }

        @Override
        public boolean await(Duration timeout) throws InterruptedException {
            return process.await(timeout);
        }
    }

    private static final class ManagedRuntimeProcess implements RuntimeProcess {
        private final Process process;
        private final Instant startedAt;
        private final LineBuffer stdout = new LineBuffer(200);
        private final LineBuffer stderr = new LineBuffer(200);
        private final AtomicBoolean gracefulStopRequested = new AtomicBoolean();
        private volatile Instant finishedAt;
        private volatile Integer exitCode;
        private volatile Thread stdoutThread;
        private volatile Thread stderrThread;

        private ManagedRuntimeProcess(Process process) {
            this.process = process;
            this.startedAt = Instant.now();
        }

        void startCapture() {
            stdoutThread =
                    new Thread(
                            () -> consume(process.getInputStream(), stdout),
                            "unifiedmc-runtime-stdout-" + process.pid());
            stderrThread =
                    new Thread(
                            () -> consume(process.getErrorStream(), stderr),
                            "unifiedmc-runtime-stderr-" + process.pid());
            stdoutThread.setDaemon(true);
            stderrThread.setDaemon(true);
            stdoutThread.start();
            stderrThread.start();
            Thread watcher =
                    new Thread(
                            () -> {
                                try {
                                    int code = process.waitFor();
                                    exitCode = code;
                                    joinCaptureThread(stdoutThread);
                                    joinCaptureThread(stderrThread);
                                    finishedAt = Instant.now();
                                } catch (InterruptedException exception) {
                                    Thread.currentThread().interrupt();
                                }
                            },
                            "unifiedmc-runtime-watcher-" + process.pid());
            watcher.setDaemon(true);
            watcher.start();
        }

        @Override
        public long pid() {
            return process.pid();
        }

        @Override
        public boolean isAlive() {
            return process.isAlive();
        }

        @Override
        public Instant startedAt() {
            return startedAt;
        }

        @Override
        public OptionalInt exitCode() {
            return exitCode == null ? OptionalInt.empty() : OptionalInt.of(exitCode);
        }

        @Override
        public List<String> stdoutTail() {
            return stdout.snapshot();
        }

        @Override
        public List<String> stderrTail() {
            return stderr.snapshot();
        }

        @Override
        public boolean await(Duration timeout) throws InterruptedException {
            boolean finished =
                    process.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (finished) {
                finalizeProcessState();
            }
            return finished;
        }

        private synchronized void finalizeProcessState() {
            if (exitCode == null) {
                exitCode = process.exitValue();
            }
            joinCaptureThread(stdoutThread);
            joinCaptureThread(stderrThread);
            if (finishedAt == null) {
                finishedAt = Instant.now();
            }
        }

        private static void joinCaptureThread(Thread thread) {
            if (thread == null) {

                return;
            }
            try {
                thread.join(1000L);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }

        void stopGracefully(Duration timeout) {
            gracefulStopRequested.set(true);
            process.destroy();
            try {
                if (!await(timeout)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }

        void destroyForcibly() {
            process.destroyForcibly();
        }

        RuntimeHealth health() {
            if (isAlive()) {
                return new RuntimeHealth(
                        gracefulStopRequested.get()
                                ? RuntimeHealthStatus.STOPPING
                                : RuntimeHealthStatus.HEALTHY,
                        Instant.now(),
                        pid(),
                        "Runtime process is alive",
                        Optional.empty());
            }
            int code = exitCode == null ? -1 : exitCode;
            RuntimeHealthStatus status =
                    code == 0 || gracefulStopRequested.get()
                            ? RuntimeHealthStatus.STOPPED
                            : RuntimeHealthStatus.FAILED;
            RuntimeCrashDiagnostics crash =
                    code == 0 || gracefulStopRequested.get()
                            ? null
                            : new RuntimeCrashDiagnostics(
                                    exitCode(),
                                    startedAt,
                                    finishedAt == null ? Instant.now() : finishedAt,
                                    stdoutTail(),
                                    stderrTail());
            return new RuntimeHealth(
                    status,
                    Instant.now(),
                    pid(),
                    status == RuntimeHealthStatus.FAILED
                            ? "Runtime exited unexpectedly with code " + code
                            : "Runtime process exited",
                    Optional.ofNullable(crash));
        }

        private static void consume(java.io.InputStream stream, LineBuffer buffer) {
            try (stream;
                    java.io.BufferedReader reader =
                            new java.io.BufferedReader(new java.io.InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.add(line);
                }
            } catch (IOException exception) {
                buffer.add("<stream capture failed: " + exception.getMessage() + ">");
            }
        }
    }

    private static final class LineBuffer {
        private final int limit;
        private final java.util.ArrayDeque<String> lines = new java.util.ArrayDeque<>();

        private LineBuffer(int limit) {
            this.limit = limit;
        }

        synchronized void add(String line) {
            if (lines.size() >= limit) {

                lines.removeFirst();
            }
            lines.addLast(line);
        }

        synchronized List<String> snapshot() {
            return List.copyOf(lines);
        }
    }
}
