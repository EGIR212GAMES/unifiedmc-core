package dev.unifiedmc.uapi.compat;

import dev.unifiedmc.uapi.Capability;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/** Deterministic capability-based evaluator for adapter declarations. */
public final class CompatibilityEvaluator {
    private CompatibilityEvaluator() {}

    public static CompatibilityReport evaluate(
            String adapterId, CompatibilityProfile profile, CompatibilityContext context) {
        List<String> unsupportedFeatures = new ArrayList<>();
        EnumSet<Capability> supported = EnumSet.noneOf(Capability.class);
        EnumSet<Capability> unsupported = EnumSet.noneOf(Capability.class);

        if (!profile.minecraftVersions().contains(context.minecraftVersion())) {
            unsupportedFeatures.add(
                    "Minecraft version "
                            + context.minecraftVersion().value()
                            + " is not supported");
        }
        if (!profile.loaders().contains(context.loader())) {
            unsupportedFeatures.add("Loader " + context.loader() + " is not supported");
        }
        if (context.serverSide() && !profile.serverSide()) {
            unsupportedFeatures.add("Adapter does not support server-side execution");
        }
        if (context.polymerRequested() && !profile.polymerRepresentation()) {
            unsupportedFeatures.add("Polymer/server-side representation is not supported");
        }
        if (context.bedrockRequested() && !profile.bedrockRepresentation()) {
            unsupportedFeatures.add("Bedrock representation is not supported");
        }
        if (profile.customJavaClient() && !context.customJavaClientAllowed()) {
            unsupportedFeatures.add("Adapter requires a custom Java client");
        }

        for (Capability capability : context.requiredCapabilities()) {
            if (profile.capabilities().contains(capability)) {
                supported.add(capability);
            } else {
                unsupported.add(capability);
                unsupportedFeatures.add(
                        "Capability " + capability + " is not declared by the adapter");
            }
        }

        CompatibilityReport.Status status;
        if (!unsupportedFeatures.isEmpty() && hasHardFailure(unsupportedFeatures)) {
            status = CompatibilityReport.Status.UNSUPPORTED;
        } else if (!unsupportedFeatures.isEmpty()) {
            status = CompatibilityReport.Status.PARTIAL;
        } else if (supported.isEmpty() && !context.requiredCapabilities().isEmpty()) {
            status = CompatibilityReport.Status.PARTIAL;
        } else {
            status = CompatibilityReport.Status.SUPPORTED;
        }

        return new CompatibilityReport(
                status,
                adapterId,
                supported,
                unsupported,
                unsupportedFeatures,
                profile.limitations());
    }

    private static boolean hasHardFailure(List<String> features) {
        return features.stream()
                .anyMatch(
                        feature ->
                                feature.startsWith("Minecraft version ")
                                        || feature.startsWith("Loader ")
                                        || feature.startsWith(
                                                "Adapter does not support server-side")
                                        || feature.startsWith(
                                                "Adapter requires a custom Java client"));
    }
}
