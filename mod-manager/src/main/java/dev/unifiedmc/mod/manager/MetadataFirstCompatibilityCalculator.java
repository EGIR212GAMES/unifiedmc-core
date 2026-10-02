package dev.unifiedmc.mod.manager;

import dev.unifiedmc.mod.ModCompatibility;
import dev.unifiedmc.mod.ModCompatibilityStatus;
import dev.unifiedmc.mod.ModEnvironment;
import dev.unifiedmc.mod.ModLoader;
import dev.unifiedmc.mod.ModMetadata;
import dev.unifiedmc.version.RuntimeVersion;
import java.util.List;
import java.util.Optional;

/**
 * Conservative compatibility calculator. It never claims Connector/adapter support without explicit
 * proof.
 */
public final class MetadataFirstCompatibilityCalculator implements ModCompatibilityCalculator {
    private final ConnectorCompatibilityProof connectorProof;
    private final AdapterCompatibilityProof adapterProof;

    public MetadataFirstCompatibilityCalculator() {
        this((m, r) -> false, (m, r) -> false);
    }

    public MetadataFirstCompatibilityCalculator(
            ConnectorCompatibilityProof connectorProof, AdapterCompatibilityProof adapterProof) {
        this.connectorProof = connectorProof;
        this.adapterProof = adapterProof;
    }

    @Override
    public ModCompatibility calculate(ModMetadata metadata, List<RuntimeVersion> runtimes) {
        if (metadata.loader() == ModLoader.UNKNOWN) {
            return invalid(
                    "Unknown loader", "Provide authoritative Fabric/Forge/NeoForge metadata.");
        }
        if (metadata.environment() == ModEnvironment.CLIENT) {
            return new ModCompatibility(
                    ModCompatibilityStatus.UNSUPPORTED,
                    Optional.empty(),
                    List.of("Client-only mod cannot be approved for a dedicated server"),
                    List.of("environment=client"));
        }
        for (RuntimeVersion runtime : runtimes) {
            if (!runtime.game().value().isBlank()
                    && matchesMinecraft(metadata, runtime.game().value())) {
                boolean loaderMatch = metadata.loader().name().equalsIgnoreCase(runtime.loader());
                if (loaderMatch) {
                    return supported(
                            runtime, "Declared loader and Minecraft version match target runtime");
                }
                if (adapterProof.proves(metadata, runtime)) {
                    return supportedViaAdapter(runtime);
                }
                if (connectorProof.proves(metadata, runtime)) {
                    return supportedViaConnector(runtime);
                }
            }
        }
        boolean legacy =
                metadata.loader() == ModLoader.FORGE
                        && metadata.minecraftVersion().map(v -> isLegacy(v.value())).orElse(false);
        if (legacy) {
            return new ModCompatibility(
                    ModCompatibilityStatus.LEGACY_BACKEND_REQUIRED,
                    Optional.empty(),
                    List.of("Metadata targets a legacy Forge generation"),
                    List.of());
        }
        return new ModCompatibility(
                ModCompatibilityStatus.UNSUPPORTED,
                Optional.empty(),
                List.of(
                        "No configured runtime matches the declared loader/Minecraft compatibility"),
                List.of());
    }

    private static boolean matchesMinecraft(ModMetadata metadata, String runtime) {
        if (metadata.minecraftVersion().isPresent()) {
            return metadata.minecraftVersion().get().value().equals(runtime);
        }
        return ModVersionConstraints.matches(runtime, metadata.minecraftVersionConstraint());
    }

    private static boolean isLegacy(String value) {
        return value.startsWith("1.7.")
                || value.startsWith("1.8.")
                || value.startsWith("1.9.")
                || value.startsWith("1.10")
                || value.startsWith("1.11")
                || value.startsWith("1.12");
    }

    private static ModCompatibility supported(RuntimeVersion runtime, String reason) {
        return new ModCompatibility(
                ModCompatibilityStatus.SUPPORTED,
                Optional.of(runtime.game().value() + ":" + runtime.loader()),
                List.of(reason),
                List.of());
    }

    private static ModCompatibility supportedViaAdapter(RuntimeVersion runtime) {
        return new ModCompatibility(
                ModCompatibilityStatus.SUPPORTED_VIA_ADAPTER,
                Optional.of(runtime.game().value() + ":" + runtime.loader()),
                List.of("Explicit adapter proof registered"),
                List.of());
    }

    private static ModCompatibility supportedViaConnector(RuntimeVersion runtime) {
        return new ModCompatibility(
                ModCompatibilityStatus.SUPPORTED_VIA_CONNECTOR,
                Optional.of(runtime.game().value() + ":" + runtime.loader()),
                List.of("Explicit Connector proof registered"),
                List.of());
    }

    private static ModCompatibility invalid(String reason, String fix) {
        return new ModCompatibility(
                ModCompatibilityStatus.INVALID, Optional.empty(), List.of(reason), List.of(fix));
    }

    @FunctionalInterface
    public interface ConnectorCompatibilityProof {
        boolean proves(ModMetadata metadata, RuntimeVersion runtime);
    }

    @FunctionalInterface
    public interface AdapterCompatibilityProof {
        boolean proves(ModMetadata metadata, RuntimeVersion runtime);
    }
}
