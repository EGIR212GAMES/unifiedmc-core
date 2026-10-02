package dev.unifiedmc.legacy;

import java.nio.file.Path;

/** Managed directory layout for experimental legacy backends. */
public final class LegacyPaths {
    private LegacyPaths() {}

    public static Path defaultRoot() {
        return Path.of("legacy");
    }

    public static Path runtime(String version) {
        return defaultRoot().resolve(version);
    }
}
