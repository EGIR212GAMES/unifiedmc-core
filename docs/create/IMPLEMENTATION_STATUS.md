# Create Adapter Implementation Status

## Implemented in this milestone

- `CreateCompatibilityProfile`
- feature support tiers and per-feature limitations
- `CreateAdapter`
- `CreateContentCompiler`
- `CreateKineticModel`
- `CreateContraptionModel` with explicit unsupported dynamic state
- `CreateBedrockAdapter`
- semantic kinetic network with deterministic propagation
- normalized persistence view for shaft/cogwheel/press state
- mechanical press semantic machine
- one pressing recipe
- unit/integration-style tests for the vertical slice

## Not implemented yet

A real Minecraft runtime hook that intercepts NeoForge block registration, player placement events, BlockEntity ticking or server packet traffic is not part of this milestone. The current repository does not yet contain a concrete NeoForge Minecraft backend process capable of hosting arbitrary modless Create semantics.

Therefore `CreateServerState.place(...)` is the validated semantic equivalent of server placement for this milestone; it must not be described as proof that an ordinary vanilla/NeoForge server already accepts real `create:shaft` packets.

The next backend integration must connect the semantic state to a real Minecraft runtime without copying Create implementation classes or assets.
