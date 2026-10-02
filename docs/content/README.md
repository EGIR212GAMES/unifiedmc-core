# Content Engine

The Content Engine consumes Universal Mod API adapters and produces backend-neutral Content IR before invoking a backend compiler.

Current pipeline:

```text
Universal Mod Adapter
  -> ANALYZE
  -> NORMALIZE
  -> VALIDATE
  -> COMPILE
  -> EMIT
  -> REGISTER
```

Current IR definitions:

- `ContentBlockDefinition`
- `ContentItemDefinition`
- `ContentEntityDefinition`
- `ContentRecipeDefinition`
- `ContentModelDefinition`
- `ContentTextureDefinition`
- `ContentBehaviorDefinition`

Current backend:

- `PolymerBackend`

The Polymer backend is intentionally declarative at this milestone. It generates deterministic artifacts under the configured output directory and reports unsupported mappings explicitly.

The repository root reserves:

```text
/generated/polymer
/generated/java
/generated/bedrock
```

No backend writes to Minecraft runtime directories directly.
