package dev.unifiedmc.runtime.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.unifiedmc.runtime.*;
import dev.unifiedmc.version.GameVersion;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;

class MinecraftRuntimeBackendAdapterTest {
    @Test
    void exposesNewRuntimeThroughExistingBackendSpi() {
        MinecraftRuntime runtime =
                new MinecraftRuntime() {
                    private final RuntimeMetadata metadata =
                            new RuntimeMetadata(
                                    "26.3:modern_neoforge",
                                    "26.3",
                                    RuntimeBackend.MODERN_NEOFORGE,
                                    "unresolved",
                                    new JavaRuntimeRequirement(
                                            new GameVersion("26.3"), 25, 25, "test"),
                                    Path.of("."),
                                    Path.of("server.jar"),
                                    true,
                                    "0".repeat(64),
                                    "official:neoforge");

                    @Override
                    public RuntimeMetadata metadata() {
                        return metadata;
                    }

                    @Override
                    public RuntimeCapabilities capabilities() {
                        return new RuntimeCapabilities(Set.of("process-isolation"));
                    }

                    @Override
                    public RuntimeHealth health() {
                        return new RuntimeHealth(
                                RuntimeHealthStatus.STOPPED,
                                java.time.Instant.now(),
                                -1,
                                "stopped",
                                java.util.Optional.empty());
                    }

                    @Override
                    public RuntimeHandle start(RuntimeRequest request) {
                        throw new UnsupportedOperationException();
                    }
                };
        var adapter = new MinecraftRuntimeBackendAdapter(runtime);
        assertEquals("neoforge", adapter.descriptor().loader());
        assertEquals("26.3", adapter.descriptor().gameVersion());
    }
}
