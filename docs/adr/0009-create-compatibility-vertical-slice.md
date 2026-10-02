# ADR 0009: Create Compatibility Vertical Slice

## Decision

Target the stable **Create 6.0.10 / Minecraft 1.21.1 / NeoForge** release for the first Create adapter milestone.

The adapter is a semantic compatibility layer, not a source fork and not a bytecode translator.

## Reasons

1. Create's official status page lists 1.21.1 as continued support and 26.1 as still in progress.
2. Create 6.0.10 is the current released 1.21.1 target.
3. Create's kinetic system is substantially larger than the initial UnifiedMC vertical slice; reproducing it incrementally is safer than copying implementation internals.
4. Create assets are under a separate All Rights Reserved license, so UnifiedMC must not bundle Create assets.

## Vertical slice

- shaft
- cogwheel
- mechanical power
- simple kinetic network
- mechanical press
- one pressing recipe

## Explicit non-goals

- full Create support
- contraptions
- Flywheel rendering parity
- Create packet compatibility
- arbitrary block/entity behavior translation
- Create asset redistribution

## Evidence

- https://wiki.createmod.net/users/development-status
- https://github.com/Creators-of-Create/Create/releases/tag/mc1.21.1-6.0.10
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/LICENSE.md
