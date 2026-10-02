package dev.unifiedmc.create.adapter;

import dev.unifiedmc.create.profile.CreateCompatibilityProfile;
import dev.unifiedmc.create.profile.CreateFeature;
import dev.unifiedmc.create.profile.CreateFeatureSupport;
import dev.unifiedmc.create.profile.CreateSupportTier;
import java.util.LinkedHashMap;
import java.util.Map;

/** Explicit Bedrock status for Create content. No automatic Polymer-to-Bedrock conversion occurs. */
public final class CreateBedrockAdapter {
    private final CreateCompatibilityProfile profile;

    public CreateBedrockAdapter(CreateCompatibilityProfile profile) {
        this.profile = profile;
    }

    public Map<String, String> diagnose() {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<CreateFeature, CreateFeatureSupport> entry : profile.features().entrySet()) {
            CreateFeatureSupport support = entry.getValue();
            if (support.tier() == CreateSupportTier.BEDROCK_REPRESENTABLE || support.bedrockVisualSupport()) {
                result.put(entry.getKey().name(), support.bedrockInteractionSupport() ? "SUPPORTED" : "PARTIAL");
            } else if (support.tier() == CreateSupportTier.UNSUPPORTED || support.tier() == CreateSupportTier.CLIENT_ONLY) {
                result.put(entry.getKey().name(), "UNSUPPORTED");
            } else {
                result.put(entry.getKey().name(), "PARTIAL");
            }
        }
        return Map.copyOf(result);
    }
}
