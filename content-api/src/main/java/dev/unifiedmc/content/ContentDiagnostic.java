package dev.unifiedmc.content;

import java.util.Objects;

/** Deterministic diagnostic produced by content analysis or compilation. */
public record ContentDiagnostic(
        Severity severity,
        String path,
        String message,
        String expected,
        String actual,
        String fix) {
    public ContentDiagnostic {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(message, "message");
        expected = expected == null ? "" : expected;
        actual = actual == null ? "" : actual;
        fix = fix == null ? "" : fix;
    }

    public enum Severity {
        INFO,
        WARNING,
        ERROR
    }
}
