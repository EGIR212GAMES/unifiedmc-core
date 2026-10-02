# Bedrock/Geyser Integration

UnifiedMC treats Geyser as a protocol/content bridge rather than a generic Java-mod translator.

## Current pipeline

```text
Universal Content
      ↓
BedrockCapabilityAnalyzer
      ↓
BedrockMappingCompiler
      +
BedrockResourcePackBuilder
      ↓
/generated/bedrock
      ↓
GeyserBridge
      ↓
Geyser custom_mappings + packs
```

The current implementation is deliberately file-based and API-version agnostic. It emits Geyser JSON mapping artifacts and a deterministic `.mcpack`; it does not embed Geyser implementation classes in the Core/UAPI modules.

See [limitations](limitations.md) for the supported and unsupported cases.
