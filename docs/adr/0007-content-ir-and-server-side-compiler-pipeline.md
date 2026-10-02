# ADR 0007: Content IR and Server-side Compiler Pipeline

## Decision

UnifiedMC Core introduces a backend-neutral Content IR between Universal Mod Adapters and content backends.

The pipeline is:

```text
Universal Mod Adapter
        |
        v
      ANALYZE
        |
        v
     NORMALIZE
        |
        v
      VALIDATE
        |
        v
      COMPILE
        |
        v
        EMIT
        |
        v
     REGISTER
```

Content IR does not import Polymer, Geyser, Minecraft, Fabric, Forge, or NeoForge APIs.

Polymer is a backend only. Its compiler may emit server-side projection artifacts and a declarative registration plan, but this milestone does not claim a live Polymer runtime integration.

## Partial compatibility

A backend must return `PARTIAL` or `UNSUPPORTED` when an IR definition or capability cannot be represented safely. Unsupported behavior is never silently dropped.

The example adapter intentionally contains an illustrative entity behavior. The current Polymer compiler reports `PARTIAL` because that generic behavior has no safe implementation in this milestone.

## Determinism

IR definitions, artifact paths, map keys, diagnostics, and registration entries are sorted deterministically. The compiler writes byte-for-byte stable JSON artifacts for identical input.
