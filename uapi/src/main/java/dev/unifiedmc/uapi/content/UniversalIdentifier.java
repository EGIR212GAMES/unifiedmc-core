package dev.unifiedmc.uapi.content;

import java.util.Objects;

/** Stable namespace/path identifier independent of any Minecraft implementation. */
public record UniversalIdentifier(String namespace, String path) {
    public UniversalIdentifier {
        requireSegment(namespace, "namespace");
        requireSegment(path, "path");
    }

    public String value() {
        return namespace + ":" + path;
    }

    private static void requireSegment(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        if (value.chars().anyMatch(Character::isWhitespace) || value.indexOf(':') >= 0) {
            throw new IllegalArgumentException(field + " must not contain whitespace or ':'");
        }
    }
}
