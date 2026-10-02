package dev.unifiedmc.legacy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.unifiedmc.runtime.JavaRuntimeDescriptor;
import dev.unifiedmc.runtime.RuntimeBackend;
import dev.unifiedmc.runtime.RuntimeInstallationManifest;
import dev.unifiedmc.runtime.RuntimeMetadata;
import dev.unifiedmc.runtime.RuntimeRequest;
import dev.unifiedmc.runtime.RuntimeTrustStore;
import dev.unifiedmc.runtime.manager.RuntimeValidator;
import dev.unifiedmc.runtime.manager.StaticRuntimeTrustStore;
import dev.unifiedmc.version.GameVersion;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LegacyRuntimeSupervisorIntegrationTest {
    @Test
    void failedLegacyProcessIsContainedAndReportsFailure() throws Exception {
        Path root = Files.createTempDirectory("unifiedmc-legacy-it-");
        Path artifact = root.resolve("server.jar");
        Files.writeString(artifact, "verified-fixture");
        String classpath = System.getProperty("java.class.path");
        String javaExecutable =
                Path.of(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java")
                        .toString();
        RuntimeInstallationManifest manifest =
                new RuntimeInstallationManifest(
                        "unifiedmc-runtime-1",
                        "legacy-fixture",
                        "1.12.2",
                        RuntimeBackend.LEGACY_FORGE,
                        "fixture",
                        21,
                        21,
                        "local:operator-approved",
                        true,
                        sha256(artifact),
                        "server.jar",
                        List.of("-cp", classpath, "dev.unifiedmc.testkit.RuntimeExitMain", "3"),
                        Instant.now(),
                        Map.of());
        RuntimeTrustStore trustStore =
                new StaticRuntimeTrustStore(
                        Map.of("local:operator-approved", manifest.artifactSha256()));
        RuntimeValidator validator = new RuntimeValidator(trustStore);
        RuntimeMetadata metadata =
                new RuntimeMetadata(
                        "legacy-fixture",
                        "1.12.2",
                        RuntimeBackend.LEGACY_FORGE,
                        "fixture",
                        new JavaRuntimeRequirement(
                                new GameVersion("1.12.2"), 21, 21, "test fixture"),
                        root,
                        Path.of("server.jar"),
                        true,
                        manifest.artifactSha256(),
                        manifest.sourceId());
        LegacyRuntime runtime = new LegacyRuntime("legacy-fixture", metadata, manifest, validator);
        InMemoryLegacyBridge bridge = new InMemoryLegacyBridge();
        JavaRuntimeDescriptor java =
                new JavaRuntimeDescriptor(
                        21,
                        Path.of(System.getProperty("java.home")),
                        Path.of(javaExecutable),
                        "fixture",
                        System.getProperty("os.arch", "unknown"),
                        "test",
                        true,
                        Set.of("process-launch"));
        RuntimeRequest request = RuntimeRequest.defaults(root, java);
        try (LegacyRuntimeSupervisor supervisor =
                new LegacyRuntimeSupervisor(
                        runtime, request, LegacyRecoveryPolicy.disabled(), bridge)) {
            supervisor.start();
            assertTrue(waitFor(() -> bridge.failed, Duration.ofSeconds(10)));
            assertTrue(bridge.errorNotified);
            assertTrue(bridge.lastDiagnostic.contains("exited unexpectedly"));
        }
    }

    private static boolean waitFor(java.util.function.BooleanSupplier condition, Duration timeout)
            throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {

                return true;
            }
            Thread.sleep(50L);
        }
        return condition.getAsBoolean();
    }

    private static String sha256(Path path) throws Exception {
        var digest = java.security.MessageDigest.getInstance("SHA-256");
        try (var input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {

                digest.update(buffer, 0, read);
            }
        }
        StringBuilder out = new StringBuilder(64);
        for (byte value : digest.digest()) {

            out.append(String.format("%02x", value));
        }
        return out.toString();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("win");
    }

    private static final class InMemoryLegacyBridge implements LegacyBridge {
        volatile boolean failed;
        volatile boolean errorNotified;
        volatile String lastDiagnostic = "";

        @Override
        public void statusChanged(String backendId, LegacyBackendStatus status, String diagnostic) {
            if (status == LegacyBackendStatus.FAILED) {

                failed = true;
            }
            lastDiagnostic = diagnostic;
        }

        @Override
        public void notifyPlayerError(LegacyPlayerError error) {
            errorNotified = true;
        }
    }
}
