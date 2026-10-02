package dev.unifiedmc.config.validation;

/** One actionable configuration diagnostic. */
public record ConfigDiagnostic(
        Severity severity,
        String path,
        String property,
        String expected,
        String actual,
        String possibleFix) {
    public enum Severity {
        ERROR,
        WARNING
    }
}
