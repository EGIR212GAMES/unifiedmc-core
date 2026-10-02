package dev.unifiedmc.runtime.manager;

import static org.junit.jupiter.api.Assertions.*;

import dev.unifiedmc.runtime.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;

class RuntimeValidatorTest {
    @Test
    void acceptsVerifiedTrustedRuntimeWithMatchingChecksum() throws Exception {
        Path root = Files.createTempDirectory("unifiedmc-runtime-");
        Path artifact = root.resolve("server.jar");
        Files.writeString(artifact, "artifact");
        var manifest = manifest(RuntimeHashing.sha256(artifact), "official:neoforge", true, 25, 25);

        var result =
                new RuntimeValidator(
                                new StaticRuntimeTrustStore(
                                        Map.of("official:neoforge", manifest.artifactSha256())))
                        .validate(root, manifest, javaRuntime(25));

        assertTrue(result.valid(), result.errors().toString());
    }

    @Test
    void rejectsChecksumMismatch() throws Exception {
        Path root = Files.createTempDirectory("unifiedmc-runtime-");
        Files.writeString(root.resolve("server.jar"), "artifact");
        var result =
                new RuntimeValidator(
                                new StaticRuntimeTrustStore(
                                        Map.of("official:neoforge", "11".repeat(32))))
                        .validate(
                                root,
                                manifest("00".repeat(32), "official:neoforge", true, 25, 25),
                                javaRuntime(25));
        assertFalse(result.valid());
        assertTrue(
                result.errors().stream()
                        .anyMatch(message -> message.contains("checksum mismatch")));
    }

    @Test
    void rejectsUntrustedSource() throws Exception {
        Path root = Files.createTempDirectory("unifiedmc-runtime-");
        Files.writeString(root.resolve("server.jar"), "artifact");
        var result =
                new RuntimeValidator(
                                new StaticRuntimeTrustStore(
                                        Map.of(
                                                "https://attacker.invalid",
                                                RuntimeHashing.sha256(root.resolve("server.jar")))))
                        .validate(
                                root,
                                manifest(
                                        RuntimeHashing.sha256(root.resolve("server.jar")),
                                        "https://attacker.invalid",
                                        true,
                                        25,
                                        25),
                                javaRuntime(25));
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(message -> message.contains("not trusted")));
    }

    @Test
    void rejectsWrongJava() throws Exception {
        Path root = Files.createTempDirectory("unifiedmc-runtime-");
        Files.writeString(root.resolve("server.jar"), "artifact");
        var manifest =
                manifest(
                        RuntimeHashing.sha256(root.resolve("server.jar")),
                        "official:neoforge",
                        true,
                        25,
                        25);
        var result =
                new RuntimeValidator(
                                new StaticRuntimeTrustStore(
                                        Map.of("official:neoforge", manifest.artifactSha256())))
                        .validate(root, manifest, javaRuntime(21));
        assertFalse(result.valid());
        assertTrue(
                result.errors().stream()
                        .anyMatch(message -> message.contains("does not satisfy Minecraft")));
    }

    private static RuntimeInstallationManifest manifest(
            String checksum, String source, boolean verified, int min, int recommended) {
        return new RuntimeInstallationManifest(
                "unifiedmc-runtime-1",
                "fixture",
                "26.3",
                RuntimeBackend.MODERN_NEOFORGE,
                "unresolved",
                min,
                recommended,
                source,
                verified,
                checksum,
                "server.jar",
                List.of("-version"),
                Instant.now(),
                Map.of());
    }

    private static JavaRuntimeDescriptor javaRuntime(int version) {
        Path home = Path.of(System.getProperty("java.home"));
        return new JavaRuntimeDescriptor(
                version,
                home,
                home.resolve("bin").resolve(isWindows() ? "java.exe" : "java"),
                "fixture",
                System.getProperty("os.arch"),
                "fixture",
                true,
                Set.of("process-launch"));
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }
}
