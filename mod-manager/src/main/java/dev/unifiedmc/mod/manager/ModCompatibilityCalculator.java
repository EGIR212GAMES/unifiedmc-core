package dev.unifiedmc.mod.manager;

import dev.unifiedmc.mod.ModCompatibility;
import dev.unifiedmc.mod.ModMetadata;
import dev.unifiedmc.version.RuntimeVersion;
import java.util.List;

/** Calculates compatibility from declared metadata and explicit policy/proof sources. */
public interface ModCompatibilityCalculator {
    ModCompatibility calculate(ModMetadata metadata, List<RuntimeVersion> runtimes);
}
