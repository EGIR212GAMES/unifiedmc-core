package dev.unifiedmc.config;

import java.util.Locale;

/** Versioned schema identifier for the UnifiedMC configuration file. */
public enum ConfigSchemaVersion {
    V1("unifiedmc-1"),
    V2("unifiedmc-2");

    private final String value;

    ConfigSchemaVersion(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ConfigSchemaVersion fromValue(String value) {
        for (ConfigSchemaVersion version : values()) {
            if (version.value.equals(value == null ? "" : value.toLowerCase(Locale.ROOT))) {
                return version;
            }
        }
        throw new IllegalArgumentException("Unsupported configuration schema: " + value);
    }
}
