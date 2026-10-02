# UnifiedMC Core

UnifiedMC Core is the control-plane foundation for a multi-runtime Minecraft Java Edition server platform.

This repository currently contains **architecture-safe APIs, module boundaries, build infrastructure and explicit capability-aware stubs**. Bedrock/Geyser artifact compilation is implemented; full protocol translation, automatic mod compatibility, and Minecraft runtime execution remain separate milestones.

## Java runtime model

**Required for UnifiedMC Core development: JDK 25+**

The main build, compiler toolchain, Gradle daemon criteria, tests, and IDE development baseline are Java 25. This is intentional because the primary target is Minecraft Java Edition 26.3 and Minecraft 26.1 introduced a Java 25 requirement. ([Minecraft 26.1](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-1))

Minecraft backends do **not** inherit the Core JDK requirement. Every isolated backend selects and launches with its own verified Java runtime:

```text
UnifiedMC Core / Gradle / tests   -> Java 25
Minecraft 26.3                    -> Java 25
Minecraft 26.2                    -> Java 25
Minecraft 26.1                    -> Java 25
Minecraft 1.21.1                  -> Java 21
Minecraft 1.20.1                  -> Java 17
Legacy generations                -> backend-specific JDK
```

Backends are separate processes and must never rely on the process-global `java` executable after Runtime Manager has selected a runtime.

See [`docs/JAVA_RUNTIME_MATRIX.md`](docs/JAVA_RUNTIME_MATRIX.md) for the verified compatibility catalog and its evidence.

## Design invariants

- Core never loads Minecraft/loader classes.
- Minecraft generations are isolated into separate backend processes.
- Compatibility is explicit and capability-driven.
- Unsupported features fail explicitly; there is no silent fallback.
- Legacy backends are not hard-wired into Core.
- The Core Java toolchain is Java 25; backend Java requirements are independent.

## Build

```bash
./gradlew clean build
./gradlew test
./gradlew spotlessCheck
./gradlew verifyArchitecture
./gradlew javaToolchains
```

Use `./gradlew :cli:run --args=doctor` (or the installed `unifiedmc doctor` distribution) to inspect discovered JDKs and backend requirements.

## Configuration

The canonical server configuration is `config/unifiedmc.toml` and uses versioned TOML schema `unifiedmc-2`.

```bash
unifiedmc config generate
unifiedmc config validate
unifiedmc config validate path/to/unifiedmc.toml
```

Use `--force` only when intentionally replacing an existing generated configuration. See [docs/config](docs/config/README.md).


## CLI lifecycle

```text
unifiedmc start
unifiedmc stop
unifiedmc restart
unifiedmc status
unifiedmc doctor
unifiedmc config validate
unifiedmc config generate
unifiedmc mods list
unifiedmc mods scan
unifiedmc versions list
unifiedmc runtimes list
```

See [docs/cli](docs/cli/README.md) for lifecycle phases and failure behavior.

## Mod Manager

The current Mod Manager is metadata-first and scans `FabricMods`, `ForgeMods`, and `NeoForgeMods` without loading mod bytecode into the Core JVM. Authoritative descriptors include Fabric's `fabric.mod.json`, modern Forge/NeoForge `META-INF/*mods.toml`, and legacy Forge `mcmod.info`. See `docs/mods/README.md`.

```text
DISCOVER -> IDENTIFY -> READ_METADATA -> DETERMINE_LOADER
        -> DETERMINE_MINECRAFT_VERSION -> DETERMINE_DEPENDENCIES
        -> CALCULATE_COMPATIBILITY -> ASSIGN_RUNTIME -> APPROVE/REJECT
```

## Universal Mod API

The `uapi` module provides backend-neutral content and adapter contracts. It separates Java, server-side, and Bedrock representations and reports `SUPPORTED`, `PARTIAL`, or `UNSUPPORTED` compatibility without loading mod bytecode. See `docs/uapi/README.md`.


## Server-side Content Engine

The `content-ir` module normalizes Universal Mod API content into a backend-neutral Content IR and runs a deterministic:

```text
ANALYZE -> NORMALIZE -> VALIDATE -> COMPILE -> EMIT -> REGISTER
```

The current `PolymerBackend` is intentionally a declarative compiler boundary. It emits deterministic artifacts and explicit registration plans, but it does not pretend to be a live Polymer runtime integration. Unsupported behavior produces `PARTIAL` or `UNSUPPORTED` results instead of silent fallback. See [docs/content](docs/content/README.md).

## Legacy backends

Legacy support is process-isolated. Forge 1.7.10 and 1.12.2 are managed under `/legacy/<version>/` and use their own JVMs; legacy support means running a legacy environment unless a dedicated compatibility adapter exists. See `docs/legacy/architecture.md`.
