package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.RuntimeBackend;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import java.util.Optional;

/** Desired runtime coordinate; installation/build identifiers are deliberately absent. */
public record RuntimeCatalogEntry(
        String gameVersion,
        RuntimeBackend backend,
        Optional<JavaRuntimeRequirement> javaRequirement) {}
