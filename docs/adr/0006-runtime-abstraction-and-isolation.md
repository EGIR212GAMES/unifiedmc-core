# ADR 0006 — Runtime abstraction and process isolation

## Decision

UnifiedMC Core models Minecraft backends as isolated runtime processes behind stable runtime interfaces. The Core JVM never loads backend/loader classes.

Runtime coordinates are `(gameVersion, backend)` and are persisted with a verified installation manifest. Java selection is per runtime instance.

## Consequences

- Modern NeoForge, Fabric-via-Connector and legacy Forge can evolve independently.
- Each runtime gets its own Java executable and working directory.
- stdout/stderr and exit codes are captured at the process boundary.
- Runtime installation is security-sensitive and therefore delegated to backend-specific trusted installers.
- The initial implementation deliberately ships a controlled installer stub rather than pretending that a generic installer can safely install every loader generation.

## Non-goals

- no generic Minecraft launcher implementation;
- no arbitrary remote URL execution;
- no shared classloader across loader generations;
- no mod compatibility implementation in this layer.
