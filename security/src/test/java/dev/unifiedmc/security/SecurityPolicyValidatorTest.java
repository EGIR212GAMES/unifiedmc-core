package dev.unifiedmc.security;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SecurityPolicyValidatorTest {
    @Test
    void requiresArtifactHashPinning() {
        var policy = new SecurityPolicy(false, true, true, false);
        assertThrows(
                IllegalArgumentException.class,
                () -> new SecurityPolicyValidator().validate(policy));
    }
}
