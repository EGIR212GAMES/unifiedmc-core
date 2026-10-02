package dev.unifiedmc.core;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/** Small dependency-free structured logger for the bootstrap/control plane. */
public final class JdkStructuredLogger implements StructuredLogger {
    private final boolean json;

    public JdkStructuredLogger(boolean json) {
        this.json = json;
    }

    @Override
    public void info(String event, Map<String, ?> fields) {
        write("INFO", event, fields, false);
    }

    @Override
    public void warn(String event, Map<String, ?> fields) {
        write("WARN", event, fields, false);
    }

    @Override
    public void error(String event, Map<String, ?> fields) {
        write("ERROR", event, fields, true);
    }

    private void write(String level, String event, Map<String, ?> fields, boolean stderr) {
        LinkedHashMap<String, Object> safe = new LinkedHashMap<>();
        safe.put("timestamp", Instant.now().toString());
        safe.put("level", level);
        safe.put("event", event);
        fields.forEach((key, value) -> safe.put(key, redact(key, value)));
        String line = json ? toJson(safe) : toText(safe);
        if (stderr) {
            System.err.println(line);
        } else {
            System.out.println(line);
        }
    }

    private static Object redact(String key, Object value) {
        String normalized = key.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("secret")
                || normalized.contains("token")
                || normalized.contains("password")
                || normalized.contains("private-key")) {
            return "<redacted>";
        }
        return value;
    }

    private static String toText(Map<String, Object> fields) {
        StringJoiner joiner = new StringJoiner(" ");
        fields.forEach((key, value) -> joiner.add(key + "=" + value));
        return joiner.toString();
    }

    private static String toJson(Map<String, Object> fields) {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        fields.forEach(
                (key, value) ->
                        joiner.add(
                                '"' + escape(key) + "\":\"" + escape(String.valueOf(value)) + '"'));
        return joiner.toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
