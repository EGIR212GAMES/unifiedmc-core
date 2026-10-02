package dev.unifiedmc.config.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

/** Artifact checksum enforcement policy. */
public enum ChecksumPolicy {
    REQUIRED("required"),
    WARN("warn"),
    DISABLED("disabled");

    private final String value;

    ChecksumPolicy(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ChecksumPolicy fromValue(String value) {
        for (ChecksumPolicy policy : values()) {
            if (policy.value.equals(value.toLowerCase(Locale.ROOT))) {
                return policy;
            }
        }
        throw new IllegalArgumentException("Unsupported checksum policy: " + value);
    }

    public String value() {
        return value;
    }
}
