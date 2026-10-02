package dev.unifiedmc.core;

import dev.unifiedmc.api.health.HealthSnapshot;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Small operator-facing status file used by separate CLI invocations. */
public final class LifecycleStateStore {
    private final Path path;

    public LifecycleStateStore(Path path) {
        this.path = path;
    }

    public void write(CoreApplicationState state, String backendId, String diagnostic)
            throws IOException {
        write(state, backendId, diagnostic, null);
    }

    public void write(
            CoreApplicationState state, String backendId, String diagnostic, HealthSnapshot health)
            throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Map<String, String> values = new LinkedHashMap<>();
        values.put("state", state.name());
        ProcessHandle current = ProcessHandle.current();
        values.put("pid", Long.toString(current.pid()));
        current.info()
                .startInstant()
                .ifPresent(value -> values.put("process-start", value.toString()));
        values.put("backend", backendId == null ? "" : backendId);
        values.put("timestamp", Instant.now().toString());
        values.put("diagnostic", diagnostic == null ? "" : diagnostic.replace('\n', ' '));
        if (health != null) {
            values.put("health", health.overall().name());
            values.put("runtime-status", health.runtime().name());
            values.put("mod-status", health.mods().name());
            values.put("compatibility-status", health.compatibility().name());
        }
        StringBuilder output = new StringBuilder();
        values.forEach(
                (key, value) -> output.append(key).append('=').append(escape(value)).append('\n'));
        Files.writeString(path, output);
    }

    public Optional<Map<String, String>> read() throws IOException {
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String line : Files.readAllLines(path)) {
            int separator = line.indexOf('=');
            if (separator > 0) {
                result.put(line.substring(0, separator), unescape(line.substring(separator + 1)));
            }
        }
        return Optional.of(Map.copyOf(result));
    }

    public Path path() {
        return path;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\n", "\\n");
    }

    private static String unescape(String value) {
        return value.replace("\\n", "\n").replace("\\\\", "\\");
    }
}
