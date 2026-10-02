package dev.unifiedmc.runtime;

import java.nio.file.Path;
import java.util.List;

/** Provider SPI for discovering concrete runtime implementations. */
public interface RuntimeProvider {
    String providerId();

    boolean supports(RuntimeBackend backend);

    List<MinecraftRuntime> discover(Path runtimeRoot);
}
