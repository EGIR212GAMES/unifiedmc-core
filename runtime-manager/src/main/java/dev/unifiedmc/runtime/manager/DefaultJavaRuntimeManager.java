package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.JavaRuntimeDescriptor;
import dev.unifiedmc.runtime.JavaRuntimeManager;
import dev.unifiedmc.runtime.JavaRuntimeValidation;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local-only Java runtime discovery and selection; installation/download is intentionally
 * unsupported.
 */
public final class DefaultJavaRuntimeManager implements JavaRuntimeManager {
    private static final Pattern PROPERTY = Pattern.compile("^\\s*([A-Za-z0-9_.-]+)\\s*=\\s*(.*)$");

    @Override
    public List<JavaRuntimeDescriptor> discover() {
        Map<Path, JavaRuntimeDescriptor> discovered = new LinkedHashMap<>();
        List<Path> homes = new ArrayList<>();
        addIfPresent(homes, System.getProperty("java.home"));
        addIfPresent(homes, System.getenv("JAVA_HOME"));
        addIfPresent(homes, System.getenv("JDK25"));
        addIfPresent(homes, System.getenv("JDK21"));
        addIfPresent(homes, System.getenv("JDK17"));
        addIfPresent(homes, System.getenv("JDK8"));

        Path javaOnPath = findOnPath("java");
        if (javaOnPath != null
                && javaOnPath.getParent() != null
                && javaOnPath.getParent().getParent() != null) {
            addIfPresent(homes, javaOnPath.getParent().getParent());
        }

        for (Path home : commonJdkHomes()) {
            addIfPresent(homes, home);
        }

        for (Path home : homes) {
            Path normalized = home.toAbsolutePath().normalize();
            if (discovered.containsKey(normalized)) {
                continue;
            }
            discover(normalized).ifPresent(descriptor -> discovered.put(normalized, descriptor));
        }

        return discovered.values().stream()
                .sorted(Comparator.comparingInt(JavaRuntimeDescriptor::javaVersion).reversed())
                .toList();
    }

    @Override
    public Optional<JavaRuntimeDescriptor> select(JavaRuntimeRequirement requirement) {
        List<JavaRuntimeDescriptor> compatible =
                discover().stream()
                        .filter(runtime -> validate(runtime, requirement).accepted())
                        .toList();
        return compatible.stream()
                .filter(runtime -> runtime.javaVersion() == requirement.recommendedJava())
                .findFirst()
                .or(
                        () ->
                                compatible.stream()
                                        .min(
                                                Comparator.comparingInt(
                                                        JavaRuntimeDescriptor::javaVersion)));
    }

    @Override
    public JavaRuntimeValidation validate(
            JavaRuntimeDescriptor runtime, JavaRuntimeRequirement requirement) {
        if (!requirement.accepts(runtime.javaVersion())) {
            return JavaRuntimeValidation.rejected(
                    "Minecraft "
                            + requirement.minecraftVersion().value()
                            + " requires Java "
                            + requirement.minimumJava()
                            + "; selected runtime is Java "
                            + runtime.javaVersion()
                            + " ("
                            + runtime.javaHome()
                            + ")");
        }
        if (!runtime.verified()) {
            return JavaRuntimeValidation.rejected(
                    "Java runtime is not verified: " + runtime.javaHome());
        }
        return JavaRuntimeValidation.accepted(
                "Java "
                        + runtime.javaVersion()
                        + " satisfies Minecraft "
                        + requirement.minecraftVersion().value());
    }

    private Optional<JavaRuntimeDescriptor> discover(Path home) {
        Path javaExecutable = executableFor(home);
        if (!Files.isRegularFile(javaExecutable)) {
            return Optional.empty();
        }
        try {
            Process process =
                    new ProcessBuilder(
                                    javaExecutable.toString(),
                                    "-XshowSettings:properties",
                                    "-version")
                            .redirectErrorStream(true)
                            .start();
            String output = new String(process.getInputStream().readAllBytes());
            int exit = process.waitFor();
            if (exit != 0) {
                return Optional.empty();
            }
            Map<String, String> properties = parseProperties(output);
            OptionalInt major = parseMajor(properties.get("java.version"));
            if (major.isEmpty()) {
                return Optional.empty();
            }
            String vendor = properties.getOrDefault("java.vendor", "unknown");
            String architecture = properties.getOrDefault("os.arch", "unknown");
            return Optional.of(
                    new JavaRuntimeDescriptor(
                            major.getAsInt(),
                            home,
                            javaExecutable,
                            vendor,
                            architecture,
                            sourceFor(home),
                            true,
                            Set.of("process-launch")));
        } catch (IOException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private static Map<String, String> parseProperties(String output) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : output.split("\\R")) {
            Matcher matcher = PROPERTY.matcher(line);
            if (matcher.matches()) {
                values.put(matcher.group(1), matcher.group(2).trim());
            }
        }
        return values;
    }

    private static OptionalInt parseMajor(String version) {
        if (version == null || version.isBlank()) {
            return OptionalInt.empty();
        }
        String[] parts = version.split("\\.");
        try {
            int first = Integer.parseInt(parts[0]);
            if (first == 1 && parts.length > 1) {
                return OptionalInt.of(Integer.parseInt(parts[1]));
            }
            return OptionalInt.of(first);
        } catch (NumberFormatException ignored) {
            return OptionalInt.empty();
        }
    }

    private static Path executableFor(Path home) {
        String name =
                System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")
                        ? "java.exe"
                        : "java";
        return home.resolve("bin").resolve(name);
    }

    private static void addIfPresent(List<Path> homes, String value) {
        if (value != null && !value.isBlank()) {
            addIfPresent(homes, Paths.get(value));
        }
    }

    private static void addIfPresent(List<Path> homes, Path value) {
        if (value != null && Files.isDirectory(value)) {
            homes.add(value);
        }
    }

    private static Path findOnPath(String command) {
        String pathValue = System.getenv("PATH");
        if (pathValue == null) {
            return null;
        }
        String executableName =
                System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")
                        ? command + ".exe"
                        : command;
        for (String part : pathValue.split(java.io.File.pathSeparator)) {
            Path candidate = Paths.get(part).resolve(executableName);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static List<Path> commonJdkHomes() {
        List<Path> paths = new ArrayList<>();
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("linux")) {
            Path base = Path.of("/usr/lib/jvm");
            if (Files.isDirectory(base)) {
                try (var stream = Files.list(base)) {
                    stream.filter(Files::isDirectory).forEach(paths::add);
                } catch (IOException ignored) {
                    // Discovery is best-effort; no installation or fallback is performed.
                }
            }
        } else if (os.contains("mac")) {
            Path base = Path.of("/Library/Java/JavaVirtualMachines");
            if (Files.isDirectory(base)) {
                try (var stream = Files.list(base)) {
                    stream.filter(Files::isDirectory)
                            .map(path -> path.resolve("Contents/Home"))
                            .forEach(paths::add);
                } catch (IOException ignored) {
                    // Discovery is best-effort; no installation or fallback is performed.
                }
            }
        }
        return paths;
    }

    private static String sourceFor(Path home) {
        if (home.toAbsolutePath()
                .normalize()
                .equals(Path.of(System.getProperty("java.home")).toAbsolutePath().normalize())) {
            return "current-jvm";
        }
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome != null
                && home.toAbsolutePath()
                        .normalize()
                        .equals(Path.of(javaHome).toAbsolutePath().normalize())) {
            return "JAVA_HOME";
        }
        return "filesystem-discovery";
    }
}
