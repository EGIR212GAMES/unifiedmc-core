package dev.unifiedmc.runtime.manager;

import static org.junit.jupiter.api.Assertions.*;

import dev.unifiedmc.runtime.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class AbstractProcessRuntimeIntegrationTest {
    @Test
    void capturesStreamsAndNonZeroExitDiagnostics() throws Exception {
        Path root = Files.createTempDirectory("unifiedmc-runtime-process-");
        Path artifact = root.resolve("server.jar");
        Files.writeString(artifact, "verified-artifact");
        String classpath = System.getProperty("java.class.path");
        var manifest =
                new RuntimeInstallationManifest(
                        "unifiedmc-runtime-1",
                        "fixture-process",
                        "1.21.1",
                        RuntimeBackend.FUTURE_CUSTOM,
                        "fixture",
                        21,
                        21,
                        "official:minecraft",
                        true,
                        RuntimeHashing.sha256(artifact),
                        "server.jar",
                        List.of("-cp", classpath, "dev.unifiedmc.testkit.RuntimeExitMain", "7"),
                        Instant.now(),
                        Map.of());
        Path javaHome = Path.of(System.getProperty("java.home"));
        var javaRuntime =
                new JavaRuntimeDescriptor(
                        21,
                        javaHome,
                        javaHome.resolve("bin").resolve(isWindows() ? "java.exe" : "java"),
                        "fixture",
                        System.getProperty("os.arch"),
                        "test",
                        true,
                        Set.of("process-launch"));
        var runtime =
                new ManifestMinecraftRuntime(
                        manifest,
                        root,
                        new RuntimeValidator(
                                new StaticRuntimeTrustStore(
                                        Map.of("official:minecraft", manifest.artifactSha256()))));
        try (RuntimeHandle handle = runtime.start(RuntimeRequest.defaults(root, javaRuntime))) {
            assertTrue(handle.await(Duration.ofSeconds(10)));
            RuntimeHealth health = handle.health();
            assertEquals(RuntimeHealthStatus.FAILED, health.status());
            var crash = health.crashDiagnostics().orElseThrow();
            assertEquals(7, crash.exitCode().orElseThrow());
            assertTrue(
                    crash.stdoutTail().stream()
                            .anyMatch(line -> line.contains("runtime-fixture-stdout")));
            assertTrue(
                    crash.stderrTail().stream()
                            .anyMatch(line -> line.contains("runtime-fixture-stderr")));
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }
}
