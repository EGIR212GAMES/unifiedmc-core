package dev.unifiedmc.api.health;

/** Aggregate health state of the UnifiedMC control plane. */
public enum HealthStatus {
    UNKNOWN,
    STARTING,
    HEALTHY,
    DEGRADED,
    UNHEALTHY,
    STOPPED
}
