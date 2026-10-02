package dev.unifiedmc.security;

/** Immutable security policy used by higher-level orchestration. */
public record SecurityPolicy(
        boolean hashPinningRequired,
        boolean sandboxBackends,
        boolean privateBackendNetwork,
        boolean allowExperimental) {}
