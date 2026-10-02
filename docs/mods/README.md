# UnifiedMC Mod Manager

The Mod Manager is deliberately metadata-first. It scans the managed `FabricMods`, `ForgeMods`, and `NeoForgeMods` directories, reads known descriptor resources, builds a dependency graph, and produces an explicit compatibility result. It never defines or executes mod classes in the Core JVM.

## Descriptor formats

- Fabric: `fabric.mod.json` at the JAR root.
- Modern Forge: `META-INF/mods.toml`.
- NeoForge: `META-INF/neoforge.mods.toml`.
- Legacy Forge: `mcmod.info` where legacy metadata is available.
- Unknown artifacts: filename fallback only; these are always `INVALID`.

Fabric's official metadata specification defines `fabric.mod.json`, including `depends`, `breaks`, `conflicts`, environment and entrypoints. Fabric also defines arrays of version constraints as OR alternatives; UnifiedMC preserves that semantics when normalizing dependencies. citeturn808265search0turn808265search1

Forge's official documentation places `mods.toml` under `META-INF` and defines `[[dependencies.<modid>]]` entries with `modId`, `mandatory`, `versionRange`, ordering and side fields. Legacy Forge 1.12.x uses `mcmod.info` metadata. citeturn657176search0turn657176search1

NeoForge likewise documents `META-INF/neoforge.mods.toml` as the mod metadata descriptor and organizes metadata and dependency configurations into TOML tables. citeturn657176search4

## Pipeline

```text
DISCOVER
  -> IDENTIFY
  -> READ_METADATA
  -> DETERMINE_LOADER
  -> DETERMINE_MINECRAFT_VERSION
  -> DETERMINE_DEPENDENCIES
  -> CALCULATE_COMPATIBILITY
  -> ASSIGN_RUNTIME
  -> APPROVE_REJECT
```

## Compatibility classes

```text
SUPPORTED
SUPPORTED_VIA_ADAPTER
SUPPORTED_VIA_CONNECTOR
LEGACY_BACKEND_REQUIRED
UNSUPPORTED
INVALID
```

`SUPPORTED_VIA_CONNECTOR` and `SUPPORTED_VIA_ADAPTER` are conservative classifications: the default implementation emits them only when an explicit proof provider says the combination is compatible. There is no implicit “probably works” path.

## CLI

```bash
unifiedmc mods scan
unifiedmc mods list
unifiedmc mods tree
unifiedmc mods doctor
unifiedmc mods scan --json
```

JSON output uses schema `unifiedmc-mod-diagnostics-1` and contains artifact identity, normalized metadata, dependency declarations, compatibility result, pipeline stages and structured diagnostics.
