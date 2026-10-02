package dev.unifiedmc.config.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

/** Compatibility strategy; implementations are intentionally explicit. */
public enum CompatibilityMode {
    STRICT("strict"),
    ADAPTER("adapter"),
    BEST_EFFORT("best-effort");

    private final String value;

    CompatibilityMode(String value) {
        this.value = value;
    }

    @JsonCreator
    public static CompatibilityMode fromValue(String value) {
        for (CompatibilityMode mode : values()) {
            if (mode.value.equals(value.toLowerCase(Locale.ROOT))) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unsupported compatibility mode: " + value);
    }

    public String value() {
        return value;
    }
}
