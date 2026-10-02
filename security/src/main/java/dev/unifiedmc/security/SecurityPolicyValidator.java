package dev.unifiedmc.security;

/** Validates a startup security policy without making assumptions about the host. */
public final class SecurityPolicyValidator {
    public void validate(SecurityPolicy policy) {
        if (!policy.hashPinningRequired()) {
            throw new IllegalArgumentException(
                    "Managed deployments must require artifact hash pinning");
        }
    }
}
