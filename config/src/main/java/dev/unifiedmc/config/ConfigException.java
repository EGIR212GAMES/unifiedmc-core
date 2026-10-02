package dev.unifiedmc.config;

import dev.unifiedmc.config.validation.ConfigDiagnostic;
import java.util.List;

/** User-facing configuration error with actionable diagnostics. */
public final class ConfigException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final transient List<ConfigDiagnostic> diagnostics;

    public ConfigException(String message, List<ConfigDiagnostic> diagnostics) {
        super(message);
        this.diagnostics = List.copyOf(diagnostics);
    }

    public List<ConfigDiagnostic> diagnostics() {
        return diagnostics;
    }
}
