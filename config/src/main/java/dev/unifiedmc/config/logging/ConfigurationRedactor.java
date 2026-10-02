package dev.unifiedmc.config.logging;

import java.util.Locale;
import java.util.Set;

/** Prevents known secret-bearing keys from being emitted verbatim. */
public final class ConfigurationRedactor {
    private static final Set<String> SECRET_FRAGMENTS =
            Set.of(
                    "password",
                    "passwd",
                    "token",
                    "secret",
                    "private-key",
                    "private_key",
                    "credential",
                    "credentials",
                    "key.pem");

    private ConfigurationRedactor() {}

    public static String redact(String property, String value) {
        String normalized = property.toLowerCase(Locale.ROOT);
        boolean sensitive = SECRET_FRAGMENTS.stream().anyMatch(normalized::contains);
        return sensitive ? "<redacted>" : value;
    }
}
