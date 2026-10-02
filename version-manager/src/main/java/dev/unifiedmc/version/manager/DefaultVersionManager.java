package dev.unifiedmc.version.manager;

import dev.unifiedmc.version.GameVersion;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import dev.unifiedmc.version.RuntimeJavaCompatibility;
import dev.unifiedmc.version.RuntimeVersion;
import java.util.List;

/** Static runtime catalog for the architectural baseline. */
public final class DefaultVersionManager implements VersionManager {
    private final RuntimeJavaCompatibility javaCompatibility =
            RuntimeJavaCompatibility.officialBaseline();

    @Override
    public List<RuntimeVersion> supportedProfiles() {
        return List.of(
                profile("1.12.2", "forge", "legacy"),
                profile("1.18.2", "forge", "legacy"),
                profile("1.20.1", "forge", "legacy"),
                profile("1.21.1", "neoforge", "pending"),
                profile("26.1", "neoforge", "pending"),
                profile("26.2", "neoforge", "pending"),
                profile("26.3", "neoforge", "stable"));
    }

    private RuntimeVersion profile(String game, String loader, String loaderVersion) {
        JavaRuntimeRequirement requirement = javaCompatibility.requirementFor(game).orElseThrow();
        return new RuntimeVersion(new GameVersion(game), loader, loaderVersion, requirement);
    }
}
