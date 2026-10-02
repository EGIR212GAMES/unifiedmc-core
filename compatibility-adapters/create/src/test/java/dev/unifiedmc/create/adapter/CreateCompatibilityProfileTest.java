package dev.unifiedmc.create.adapter;

import static org.junit.jupiter.api.Assertions.*;

import dev.unifiedmc.create.profile.CreateCompatibilityProfile;
import dev.unifiedmc.create.profile.CreateFeature;
import dev.unifiedmc.create.profile.CreateSupportTier;
import org.junit.jupiter.api.Test;

class CreateCompatibilityProfileTest {
    @Test
    void targetsReleasedStableCreate6On1211() {
        CreateCompatibilityProfile profile = new CreateCompatibilityProfile();
        assertEquals("6.0.10", CreateCompatibilityProfile.CREATE_VERSION);
        assertEquals("1.21.1", CreateCompatibilityProfile.MINECRAFT_VERSION);
        assertEquals(
                CreateSupportTier.SERVER_SIDE_EMULATABLE,
                profile.features().get(CreateFeature.SHAFT).tier());
        assertEquals(
                CreateSupportTier.UNSUPPORTED,
                profile.features().get(CreateFeature.CONTRAPTIONS).tier());
        assertTrue(profile.dependencies().contains("flywheel"));
    }
}
