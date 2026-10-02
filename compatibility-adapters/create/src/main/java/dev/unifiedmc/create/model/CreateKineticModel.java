package dev.unifiedmc.create.model;

import java.util.Objects;

/** Backend-neutral semantic state of one Create kinetic node. */
public record CreateKineticModel(
        String id,
        Kind kind,
        int x,
        int y,
        int z,
        Axis axis,
        double speed,
        Long networkId,
        boolean powered) {
    public CreateKineticModel {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(axis, "axis");
    }

    public enum Kind {
        SHAFT,
        COGWHEEL,
        MECHANICAL_PRESS
    }

    public enum Axis { X, Y, Z }
}
