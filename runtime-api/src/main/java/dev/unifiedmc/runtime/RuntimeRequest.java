package dev.unifiedmc.runtime;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Launch request for an isolated runtime process. */
public record RuntimeRequest(
        Path workingDirectory,
        JavaRuntimeDescriptor javaRuntime,
        List<String> jvmArguments,
        List<String> applicationArguments,
        Map<String, String> environment,
        Duration startupTimeout,
        Duration shutdownTimeout) {
    public RuntimeRequest {
        Objects.requireNonNull(workingDirectory, "workingDirectory");
        Objects.requireNonNull(javaRuntime, "javaRuntime");
        Objects.requireNonNull(jvmArguments, "jvmArguments");
        Objects.requireNonNull(applicationArguments, "applicationArguments");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(startupTimeout, "startupTimeout");
        Objects.requireNonNull(shutdownTimeout, "shutdownTimeout");
        if (startupTimeout.isNegative() || startupTimeout.isZero()) {
            throw new IllegalArgumentException("startupTimeout must be positive");
        }
        if (shutdownTimeout.isNegative() || shutdownTimeout.isZero()) {
            throw new IllegalArgumentException("shutdownTimeout must be positive");
        }
        jvmArguments = List.copyOf(jvmArguments);
        applicationArguments = List.copyOf(applicationArguments);
        environment = Map.copyOf(environment);
    }

    public static RuntimeRequest defaults(
            Path workingDirectory, JavaRuntimeDescriptor javaRuntime) {
        return new RuntimeRequest(
                workingDirectory,
                javaRuntime,
                List.of(),
                List.of(),
                Map.of(),
                Duration.ofSeconds(30),
                Duration.ofSeconds(15));
    }
}
