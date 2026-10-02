package dev.unifiedmc.core;

import java.util.Map;

/** Minimal structured logging contract used by the orchestration layer. */
public interface StructuredLogger {
    void info(String event, Map<String, ?> fields);

    void warn(String event, Map<String, ?> fields);

    void error(String event, Map<String, ?> fields);
}
