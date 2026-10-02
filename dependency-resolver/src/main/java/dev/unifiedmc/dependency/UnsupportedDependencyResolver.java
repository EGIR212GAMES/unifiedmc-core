package dev.unifiedmc.dependency;

import dev.unifiedmc.mod.ModDescriptor;
import java.util.List;

/** Explicitly incomplete resolver until loader-aware dependency graphs are implemented. */
public final class UnsupportedDependencyResolver implements DependencyResolver {
    @Override
    public ResolutionResult resolve(List<ModDescriptor> mods, String gameVersion, String loader) {
        if (mods.isEmpty()) {
            return new ResolutionResult(ResolutionResult.Status.RESOLVED, List.of());
        }
        return new ResolutionResult(
                ResolutionResult.Status.INCOMPLETE,
                List.of("Loader-aware dependency resolution is not implemented yet"));
    }
}
