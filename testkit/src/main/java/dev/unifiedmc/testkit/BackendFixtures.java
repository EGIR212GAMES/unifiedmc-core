package dev.unifiedmc.testkit;

import dev.unifiedmc.runtime.BackendCapabilities;
import dev.unifiedmc.runtime.BackendDescriptor;
import dev.unifiedmc.runtime.BackendId;
import dev.unifiedmc.runtime.BackendLaunchRequest;
import dev.unifiedmc.runtime.BackendState;
import dev.unifiedmc.runtime.JavaRuntimeDescriptor;
import dev.unifiedmc.runtime.JavaRuntimeManager;
import dev.unifiedmc.runtime.JavaRuntimeValidation;
import dev.unifiedmc.runtime.UnifiedBackend;
import dev.unifiedmc.version.GameVersion;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Small deterministic runtime fixtures; they never start Minecraft. */
public final class BackendFixtures {
    private BackendFixtures() {}

    public static UnifiedBackend stoppedBackend() {
        return new CapturingBackend(false);
    }

    public static CapturingBackend capturingBackend() {
        return new CapturingBackend(true);
    }

    public static JavaRuntimeManager java25Manager() {
        Path home = Path.of(System.getProperty("java.home"));
        Path javaExecutable = home.resolve("bin").resolve(isWindows() ? "java.exe" : "java");
        JavaRuntimeDescriptor descriptor =
                new JavaRuntimeDescriptor(
                        25,
                        home,
                        javaExecutable,
                        "fixture",
                        System.getProperty("os.arch", "unknown"),
                        "fixture",
                        true,
                        Set.of("process-launch"));
        return new FixedJavaRuntimeManager(descriptor);
    }

    public static JavaRuntimeManager java21Manager() {
        Path home = Path.of(System.getProperty("java.home"));
        Path javaExecutable = home.resolve("bin").resolve(isWindows() ? "java.exe" : "java");
        JavaRuntimeDescriptor descriptor =
                new JavaRuntimeDescriptor(
                        21,
                        home,
                        javaExecutable,
                        "fixture",
                        System.getProperty("os.arch", "unknown"),
                        "fixture",
                        true,
                        Set.of("process-launch"));
        return new FixedJavaRuntimeManager(descriptor);
    }

    public static CapturingBackend java21CapturingBackend() {
        return new CapturingBackend(
                new BackendDescriptor(
                        new BackendId("fixture-1.21.1"),
                        "1.21.1",
                        "neoforge",
                        "fixture",
                        new JavaRuntimeRequirement(new GameVersion("1.21.1"), 21, 21, "fixture")),
                true);
    }

    public static JavaRuntimeManager emptyJavaRuntimeManager() {
        return new FixedJavaRuntimeManager(null);
    }

    public static BackendLaunchRequest launchRequest() {
        return new BackendLaunchRequest(Path.of("."), List.of(), List.of(), java.util.Map.of());
    }

    public static final class CapturingBackend implements UnifiedBackend {
        private final BackendDescriptor descriptor;
        private final boolean capture;
        private JavaRuntimeDescriptor selectedJava;

        private CapturingBackend(boolean capture) {
            this(
                    new BackendDescriptor(
                            new BackendId("fixture"),
                            "26.3",
                            "fixture",
                            "1",
                            new JavaRuntimeRequirement(new GameVersion("26.3"), 25, 25, "fixture")),
                    capture);
        }

        private CapturingBackend(BackendDescriptor descriptor, boolean capture) {
            this.descriptor = descriptor;
            this.capture = capture;
        }

        public Optional<JavaRuntimeDescriptor> selectedJava() {
            return Optional.ofNullable(selectedJava);
        }

        @Override
        public BackendDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public BackendCapabilities capabilities() {
            return new BackendCapabilities(Set.of("fixture"));
        }

        @Override
        public BackendState state() {
            return BackendState.STOPPED;
        }

        @Override
        public void start(JavaRuntimeDescriptor javaRuntime, BackendLaunchRequest request) {
            if (capture) {
                selectedJava = javaRuntime;
            } else {
                throw new UnsupportedOperationException("Fixture does not start a backend process");
            }
        }

        @Override
        public void stop() {
            // No process exists in the fixture.
        }
    }

    private static final class FixedJavaRuntimeManager implements JavaRuntimeManager {
        private final JavaRuntimeDescriptor descriptor;

        private FixedJavaRuntimeManager(JavaRuntimeDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public List<JavaRuntimeDescriptor> discover() {
            return descriptor == null ? List.of() : List.of(descriptor);
        }

        @Override
        public Optional<JavaRuntimeDescriptor> select(JavaRuntimeRequirement requirement) {
            if (descriptor == null) {
                return Optional.empty();
            }
            return validate(descriptor, requirement).accepted()
                    ? Optional.of(descriptor)
                    : Optional.empty();
        }

        @Override
        public JavaRuntimeValidation validate(
                JavaRuntimeDescriptor runtime, JavaRuntimeRequirement requirement) {
            if (runtime.javaVersion() >= requirement.minimumJava()) {
                return JavaRuntimeValidation.accepted("fixture");
            }
            return JavaRuntimeValidation.rejected(
                    "Minecraft "
                            + requirement.minecraftVersion().value()
                            + " requires Java "
                            + requirement.minimumJava()
                            + "; selected runtime is Java "
                            + runtime.javaVersion());
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
