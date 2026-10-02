package dev.unifiedmc.mod;

import java.nio.file.Path;
import java.util.Objects;

/** Physical mod artifact. The manager may inspect archive metadata but never loads its bytecode. */
public record ModArtifact(Path path, String sha256, ModLoader directoryLoaderHint, long sizeBytes) {
    public ModArtifact {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(sha256, "sha256");
        Objects.requireNonNull(directoryLoaderHint, "directoryLoaderHint");
    }
}
