package dev.unifiedmc.create.adapter;

import static org.junit.jupiter.api.Assertions.*;

import dev.unifiedmc.create.profile.CreateCompatibilityProfile;
import org.junit.jupiter.api.Test;

class CreateBedrockAdapterTest {
    @Test
    void bedrockStatusIsExplicit() {
        var result = new CreateBedrockAdapter(new CreateCompatibilityProfile()).diagnose();
        assertEquals("SUPPORTED", result.get("SHAFT"));
        assertEquals("PARTIAL", result.get("BEDROCK_INTERACTION"));
        assertEquals("UNSUPPORTED", result.get("CONTRAPTIONS"));
    }
}
