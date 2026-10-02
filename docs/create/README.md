# Create Compatibility Adapter

The first Create adapter is intentionally bounded to a semantic vertical slice on Create 6.0.10 / Minecraft 1.21.1 / NeoForge.

Implemented semantics:

- shafts;
- small cogwheel concept;
- explicit mechanical power source;
- simple axis-aware kinetic propagation;
- persistent normalized kinetic state;
- mechanical press semantic machine;
- one deterministic pressing recipe;
- explicit Java/server/Bedrock compatibility profile.

Not implemented:

- full Create registry/runtime integration;
- Create client rendering/Flywheel execution;
- full stress configuration;
- belt/basin automation;
- contraption assembly/collision/actors;
- Create packet protocol reimplementation;
- Create-specific assets;
- automatic translation of arbitrary Create content.

The current implementation is a compatibility semantic kernel and adapter contract. It does not yet make a vanilla Minecraft server binary execute Create gameplay. That requires a concrete backend runtime adapter, which is intentionally a later milestone.
