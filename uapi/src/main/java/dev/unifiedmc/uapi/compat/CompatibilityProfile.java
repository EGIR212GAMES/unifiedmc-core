package dev.unifiedmc.uapi.compat;

import dev.unifiedmc.mod.ModLoader;
import dev.unifiedmc.uapi.Capability;
import dev.unifiedmc.version.GameVersion;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Declarative compatibility contract for a Universal Mod API adapter. */
public record CompatibilityProfile(
        Set<Capability> capabilities,
        Set<GameVersion> minecraftVersions,
        Set<ModLoader> loaders,
        boolean serverSide,
        boolean polymerRepresentation,
        boolean bedrockRepresentation,
        boolean customJavaClient,
        List<String> limitations) {
    public CompatibilityProfile {
        capabilities = Set.copyOf(capabilities);
        minecraftVersions = Set.copyOf(minecraftVersions);
        loaders = Set.copyOf(loaders);
        limitations = List.copyOf(limitations);
        if (serverSide != capabilities.contains(Capability.SERVER_SIDE)) {
            throw new IllegalArgumentException("serverSide flag must match SERVER_SIDE capability");
        }
        if (polymerRepresentation != capabilities.contains(Capability.POLYMER_REPRESENTATION)) {
            throw new IllegalArgumentException(
                    "polymerRepresentation flag must match POLYMER_REPRESENTATION capability");
        }
        if (bedrockRepresentation != capabilities.contains(Capability.BEDROCK)) {
            throw new IllegalArgumentException(
                    "bedrockRepresentation flag must match BEDROCK capability");
        }
        if (customJavaClient != capabilities.contains(Capability.CUSTOM_JAVA_CLIENT)) {
            throw new IllegalArgumentException(
                    "customJavaClient flag must match CUSTOM_JAVA_CLIENT capability");
        }
    }

    public boolean supports(GameVersion version, ModLoader loader) {
        return minecraftVersions.contains(version) && loaders.contains(loader);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Set<Capability> capabilities = EnumSet.noneOf(Capability.class);
        private final Set<GameVersion> minecraftVersions = new java.util.LinkedHashSet<>();
        private final Set<ModLoader> loaders = EnumSet.noneOf(ModLoader.class);
        private final List<String> limitations = new ArrayList<>();
        private boolean serverSide;
        private boolean polymerRepresentation;
        private boolean bedrockRepresentation;
        private boolean customJavaClient;

        public Builder capability(Capability capability) {
            capabilities.add(Objects.requireNonNull(capability, "capability"));
            return this;
        }

        public Builder capabilities(Capability... values) {
            for (Capability value : values) {

                capability(value);
            }
            return this;
        }

        public Builder minecraftVersion(String version) {
            minecraftVersions.add(new GameVersion(version));
            return this;
        }

        public Builder loader(ModLoader loader) {
            loaders.add(Objects.requireNonNull(loader, "loader"));
            return this;
        }

        public Builder serverSide(boolean value) {
            serverSide = value;
            if (value) {

                capability(Capability.SERVER_SIDE);
            }
            return this;
        }

        public Builder polymerRepresentation(boolean value) {
            polymerRepresentation = value;
            if (value) {

                capability(Capability.POLYMER_REPRESENTATION);
            }
            return this;
        }

        public Builder bedrockRepresentation(boolean value) {
            bedrockRepresentation = value;
            if (value) {

                capability(Capability.BEDROCK);
            }
            return this;
        }

        public Builder customJavaClient(boolean value) {
            customJavaClient = value;
            if (value) {

                capability(Capability.CUSTOM_JAVA_CLIENT);
            }
            return this;
        }

        public Builder limitation(String limitation) {
            limitations.add(Objects.requireNonNull(limitation, "limitation"));
            return this;
        }

        public CompatibilityProfile build() {
            return new CompatibilityProfile(
                    capabilities,
                    minecraftVersions,
                    loaders,
                    serverSide,
                    polymerRepresentation,
                    bedrockRepresentation,
                    customJavaClient,
                    limitations);
        }
    }
}
