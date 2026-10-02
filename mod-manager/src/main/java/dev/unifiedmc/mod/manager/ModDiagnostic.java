package dev.unifiedmc.mod.manager;

import java.util.Objects;

/** GUI/launcher-safe diagnostic object. */
public record ModDiagnostic(
        String code, String severity, String path, String property, String message, String fix) {
    public ModDiagnostic {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(property, "property");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(fix, "fix");
    }
}
