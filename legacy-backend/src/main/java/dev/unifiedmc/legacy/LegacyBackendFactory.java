package dev.unifiedmc.legacy;

import dev.unifiedmc.runtime.BackendDescriptor;
import dev.unifiedmc.runtime.RuntimeInstallationManifest;
import dev.unifiedmc.runtime.RuntimeMetadata;
import dev.unifiedmc.runtime.RuntimeValidationResult;
import dev.unifiedmc.runtime.RuntimeValidator;
import java.util.Objects;

/** Factory for isolated legacy Forge backends from verified runtime manifests. */
public final class LegacyBackendFactory {
    private final RuntimeValidator validator;
    private final LegacyBridge bridge;

    public LegacyBackendFactory(RuntimeValidator validator, LegacyBridge bridge) {
        this.validator = Objects.requireNonNull(validator, "validator");
        this.bridge = Objects.requireNonNull(bridge, "bridge");
    }

    public LegacyBackend create(
            RuntimeMetadata metadata,
            RuntimeInstallationManifest manifest,
            BackendDescriptor descriptor,
            LegacyRecoveryPolicy recoveryPolicy) {
        RuntimeValidationResult validation =
                validator.validateManifestOnly(metadata.rootDirectory(), manifest);
        if (!validation.valid()) {
            throw new IllegalArgumentException(
                    "Invalid legacy runtime manifest: " + String.join("; ", validation.errors()));
        }
        LegacyRuntime runtime =
                new LegacyRuntime(descriptor.id().value(), metadata, manifest, validator);
        return new LegacyBackend(descriptor, runtime, recoveryPolicy, bridge);
    }
}
