package dev.unifiedmc.create.profile;

import java.util.List;
import java.util.Objects;

/** Explicit support statement; limitations are never silently collapsed. */
public record CreateFeatureSupport(
        CreateFeature feature,
        CreateSupportTier tier,
        boolean functionalSupport,
        boolean javaVisualSupport,
        boolean bedrockVisualSupport,
        boolean bedrockInteractionSupport,
        boolean persistenceSupport,
        String networkingComplexity,
        List<String> knownLimitations) {
    public CreateFeatureSupport {
        Objects.requireNonNull(feature, "feature");
        Objects.requireNonNull(tier, "tier");
        Objects.requireNonNull(networkingComplexity, "networkingComplexity");
        knownLimitations = List.copyOf(knownLimitations);
    }
}
