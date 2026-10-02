# UnifiedMC Bedrock/Geyser limitations

## Source of truth

This layer follows the current Geyser documentation and source behavior rather than assuming Java content can be converted automatically.

- Geyser does not convert Java Edition resource packs into Bedrock resource packs. Custom Bedrock resource packs must be supplied explicitly. [Geyser resource packs](https://geysermc.org/wiki/geyser/packs/)
- Geyser does not generate custom block mappings automatically. Custom mappings or a Geyser extension are required. [Geyser custom blocks](https://geysermc.org/wiki/geyser/custom-blocks/)
- Custom items require explicit mappings plus a Bedrock resource pack. Non-vanilla Java items require a Geyser API/extension registration path and cannot be registered with JSON mappings alone. [Geyser custom items](https://geysermc.org/wiki/geyser/custom-items/)
- Geyser extensions can register custom items and blocks and provide the backend-specific behavior that JSON mappings cannot express. [Geyser extensions](https://geysermc.org/wiki/geyser/extensions/)
- Geyser itself states that modded servers are only compatible through Geyser when a vanilla client can join; most arbitrary mod-added blocks/items/features cannot be translated automatically. [Geyser FAQ](https://geysermc.org/wiki/geyser/faq/)

## UnifiedMC policy

UnifiedMC therefore treats Bedrock projection as an explicit compiler pipeline:

```text
Universal Content
    ↓
BedrockCapabilityAnalyzer
    ↓
BedrockMappingCompiler
    +
BedrockResourcePackBuilder
    ↓
GeyserBridge
```

No Java resource-pack conversion is assumed.

No arbitrary mod behavior translation is assumed.

A capability may be `SUPPORTED`, `PARTIAL`, or `UNSUPPORTED`. `PARTIAL` is never silently promoted to `SUPPORTED`.

## Current milestone

Implemented:

- explicit Geyser custom block mappings;
- vanilla-based custom item JSON v2 mappings when `bedrock-base-item` is explicitly declared;
- deterministic Bedrock resource-pack archive generation;
- diagnostics and deployment to a Geyser `custom_mappings`/`packs` directory;
- explicit detection of cases requiring a Geyser extension.

Not implemented:

- version-pinned Geyser extension generation;
- automatic Java resource-pack conversion;
- generic entity behavior translation;
- arbitrary modded item registration without an extension;
- automatic Java→Bedrock mechanics conversion.
