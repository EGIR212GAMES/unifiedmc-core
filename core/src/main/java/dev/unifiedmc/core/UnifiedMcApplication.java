package dev.unifiedmc.core;

import dev.unifiedmc.api.health.HealthSnapshot;
import java.nio.file.Path;

/** Main orchestration API for the UnifiedMC Core process. */
public interface UnifiedMcApplication {
    void start(Path configurationPath);

    void stop();

    default void restart(Path configurationPath) {
        stop();
        start(configurationPath);
    }

    CoreApplicationState state();

    HealthSnapshot health();

    StartupDiagnostics diagnostics();

    void awaitTermination() throws InterruptedException;
}
