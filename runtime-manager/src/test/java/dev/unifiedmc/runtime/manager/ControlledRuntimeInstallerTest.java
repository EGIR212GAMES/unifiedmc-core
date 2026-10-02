package dev.unifiedmc.runtime.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.unifiedmc.runtime.*;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ControlledRuntimeInstallerTest {
    @Test
    void refusesToInstallWithoutTrustedInstaller() {
        var result =
                new ControlledRuntimeInstaller()
                        .install(
                                new RuntimeInstallationRequest(
                                        "26.3",
                                        RuntimeBackend.MODERN_NEOFORGE,
                                        Path.of("runtimes")));
        assertEquals(RuntimeInstallationStatus.UNSUPPORTED, result.status());
    }
}
