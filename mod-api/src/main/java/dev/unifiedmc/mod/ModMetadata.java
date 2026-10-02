package dev.unifiedmc.mod;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Normalized metadata model across Fabric, Forge/NeoForge and legacy Forge descriptors. */
public record ModMetadata(
        ModArtifact artifact,
        ModId id,
        String version,
        String name,
        ModLoader loader,
        ModEnvironment environment,
        Optional<MinecraftVersion> minecraftVersion,
        String minecraftVersionConstraint,
        String loaderVersionConstraint,
        List<ModDependency> dependencies,
        List<ModCapability> capabilities,
        String metadataFormat) {
    public ModMetadata {
        Objects.requireNonNull(artifact, "artifact");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(minecraftVersion, "minecraftVersion");
        Objects.requireNonNull(minecraftVersionConstraint, "minecraftVersionConstraint");
        Objects.requireNonNull(loaderVersionConstraint, "loaderVersionConstraint");
        Objects.requireNonNull(metadataFormat, "metadataFormat");
        dependencies = List.copyOf(dependencies);
        capabilities = List.copyOf(capabilities);
    }
}
