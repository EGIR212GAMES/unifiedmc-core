package dev.unifiedmc.geyser;

import java.util.Objects;

/** Deterministic diagnostic explaining a Bedrock projection decision. */
public record BedrockDiagnostic(
        Severity severity,
        String path,
        String message,
        String expected,
        String actual,
        String fix) {
    public BedrockDiagnostic {
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
