package dev.unifiedmc.runtime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

/** Describes a discovered Java installation that can be selected for a runtime process. */
public record JavaRuntimeDescriptor(
        int javaVersion,
        Path javaHome,
        Path javaExecutable,
        String vendor,
        String architecture,
        String source,
        boolean verified,
        Set<String> capabilities) {
    public JavaRuntimeDescriptor {
        if (javaVersion < 8) {
            throw new IllegalArgumentException("Invalid Java major version: " + javaVersion);
        }
        Objects.requireNonNull(javaHome, "javaHome");
        Objects.requireNonNull(javaExecutable, "javaExecutable");
        Objects.requireNonNull(vendor, "vendor");
        Objects.requireNonNull(architecture, "architecture");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(capabilities, "capabilities");
        if (!Files.isDirectory(javaHome)) {
            throw new IllegalArgumentException("Java home is not a directory: " + javaHome);
        }
        if (!Files.isRegularFile(javaExecutable)) {
            throw new IllegalArgumentException("Java executable does not exist: " + javaExecutable);
        }
        capabilities = Set.copyOf(capabilities);
    }

    public boolean supports(String capability) {
        return capabilities.contains(Objects.requireNonNull(capability, "capability"));
    }
}
