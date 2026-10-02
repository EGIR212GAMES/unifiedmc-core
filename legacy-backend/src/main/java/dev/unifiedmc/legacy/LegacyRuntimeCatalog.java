package dev.unifiedmc.legacy;

import dev.unifiedmc.runtime.RuntimeBackend;
import dev.unifiedmc.version.GameVersion;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import dev.unifiedmc.version.RuntimeVersion;
import java.util.List;
import java.util.Optional;

/** Experimental legacy runtime coordinates with no Forge build numbers hard-coded. */
public final class LegacyRuntimeCatalog {
    public List<RuntimeVersion> entries() {
        return List.of(
                new RuntimeVersion(
                        new GameVersion("1.12.2"), "forge", "PIN_REQUIRED", requirement("1.12.2")),
                new RuntimeVersion(
                        new GameVersion("1.7.10"),
                        "forge",
                        "PIN_REQUIRED",
                        new JavaRuntimeRequirement(
                                new GameVersion("1.7.10"),
                                8,
                                8,
                                "historical operator baseline; verify against the selected Forge build")));
    }

    public Optional<RuntimeVersion> find(String version) {
        return entries().stream().filter(entry -> entry.game().value().equals(version)).findFirst();
    }

    public RuntimeBackend backend() {
        return RuntimeBackend.LEGACY_FORGE;
    }

    private JavaRuntimeRequirement requirement(String version) {
        return new JavaRuntimeRequirement(
                new GameVersion(version),
                8,
                8,
                "https://docs.minecraftforge.net/en/fg-5.x/gettingstarted/");
    }
}
