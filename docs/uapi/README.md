# Universal Mod API

UnifiedMC UAPI is a backend-neutral compatibility contract for normalizing mod content.

The API deliberately does not expose `net.minecraft`, Fabric, Forge, NeoForge, Geyser, or
Polymer classes. An adapter describes semantic capabilities and can expose independent
representations for:

```text
Universal content
├── Java representation
├── Server-side representation
└── Bedrock representation
```

## Compatibility semantics

Adapters declare a `CompatibilityProfile`. A `CompatibilityContext` describes one requested
target. `CompatibilityEvaluator` produces `CompatibilityReport` with exactly one of:

- `SUPPORTED`
- `PARTIAL`
- `UNSUPPORTED`

Unsupported capabilities are listed explicitly. Partial support is never promoted to supported.

## Adapter boundary

```text
Mod JAR
  |
  v
ModAdapter
  |
  +--> UniversalContentBundle
  |
  +--> UniversalRepresentationBundle
         |
         +--> JavaRepresentation
         +--> ServerSideRepresentation
         +--> BedrockRepresentation
```

The example adapter in `examples/uapi-adapter` demonstrates a block, item, recipe and entity.
It is illustrative only and does not execute any Minecraft code.

Create is intentionally not integrated at this stage.
