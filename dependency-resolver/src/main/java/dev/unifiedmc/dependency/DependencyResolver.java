package dev.unifiedmc.dependency;

import dev.unifiedmc.mod.ModDescriptor;
import java.util.List;

/** Resolves a set of mod metadata into a runtime-specific dependency plan. */
public interface DependencyResolver {
    ResolutionResult resolve(List<ModDescriptor> mods, String gameVersion, String loader);
}
