package dev.unifiedmc.runtime.manager;

import java.nio.file.Path;

/** Standard managed runtime registry location. */
public final class RuntimePaths {
    private RuntimePaths() {}

    public static Path defaultRoot() {
        return Path.of("runtimes");
    }
}
