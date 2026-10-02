package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.BackendLaunchRequest;
import dev.unifiedmc.runtime.JavaRuntimeDescriptor;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Launches a backend process with the Java executable selected by Core. */
public final class JavaProcessLauncher {
    public Process launch(JavaRuntimeDescriptor runtime, BackendLaunchRequest request)
            throws IOException {
        ProcessBuilder builder =
                new ProcessBuilder(buildCommand(runtime, request))
                        .directory(request.workingDirectory().toFile());
        builder.environment().putAll(request.environment());
        return builder.start();
    }

    /**
     * Returns the exact process command; the first entry is always the selected Java executable.
     */
    public List<String> buildCommand(JavaRuntimeDescriptor runtime, BackendLaunchRequest request) {
        List<String> command = new ArrayList<>();
        command.add(runtime.javaExecutable().toString());
        command.addAll(request.jvmArguments());
        command.addAll(request.applicationArguments());
        return List.copyOf(command);
    }

    public Path selectedExecutable(JavaRuntimeDescriptor runtime) {
        return runtime.javaExecutable();
    }
}
