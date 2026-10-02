package dev.unifiedmc.uapi.compat;

import dev.unifiedmc.mod.ModLoader;
import dev.unifiedmc.uapi.Capability;
import dev.unifiedmc.version.GameVersion;
import java.util.Set;

/** Inputs used when evaluating one adapter against one runtime/client target. */
public record CompatibilityContext(
        GameVersion minecraftVersion,
        ModLoader loader,
        boolean serverSide,
        boolean polymerRequested,
        boolean bedrockRequested,
        boolean customJavaClientAllowed,
        Set<Capability> requiredCapabilities) {
    public CompatibilityContext {
        requiredCapabilities = Set.copyOf(requiredCapabilities);
    }

    public static CompatibilityContext server(GameVersion version, ModLoader loader) {
        return new CompatibilityContext(version, loader, true, false, false, true, Set.of());
    }
}
