package dev.unifiedmc.runtime;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Runtime-neutral launch request; the selected Java executable is supplied by Core. */
public record BackendLaunchRequest(
        Path workingDirectory,
        List<String> jvmArguments,
        List<String> applicationArguments,
        Map<String, String> environment) {
    public BackendLaunchRequest {
        Objects.requireNonNull(workingDirectory, "workingDirectory");
        Objects.requireNonNull(jvmArguments, "jvmArguments");
        Objects.requireNonNull(applicationArguments, "applicationArguments");
        Objects.requireNonNull(environment, "environment");
        jvmArguments = List.copyOf(jvmArguments);
        applicationArguments = List.copyOf(applicationArguments);
        environment = Map.copyOf(environment);
    }
}
