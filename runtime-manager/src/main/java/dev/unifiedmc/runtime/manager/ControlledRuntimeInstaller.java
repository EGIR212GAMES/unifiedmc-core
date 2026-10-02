package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.RuntimeInstallationRequest;
import dev.unifiedmc.runtime.RuntimeInstallationResult;
import dev.unifiedmc.runtime.RuntimeInstallationStatus;
import dev.unifiedmc.runtime.RuntimeInstaller;

/**
 * Safe placeholder installer. It never downloads or executes artifacts until an official installer
 * is registered.
 */
public final class ControlledRuntimeInstaller implements RuntimeInstaller {
    @Override
    public RuntimeInstallationResult install(RuntimeInstallationRequest request) {
        return new RuntimeInstallationResult(
                RuntimeInstallationStatus.UNSUPPORTED,
                "No trusted installer is registered for "
                        + request.gameVersion()
                        + " / "
                        + request.backend(),
                java.util.List.of(
                        "No network download was attempted",
                        "No arbitrary URL was accepted",
                        "Register an official backend installer before enabling installation for this runtime"));
    }
}
