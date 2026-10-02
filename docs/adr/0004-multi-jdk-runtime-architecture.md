# ADR 0004: Multi-JDK Runtime Architecture

- Status: Accepted
- Date: 2026-09-30
- Scope: UnifiedMC Core and isolated Minecraft backend processes

## Decision

**UnifiedMC Core uses Java 25 as its primary build/runtime target, while individual Minecraft backend processes select their own required Java runtime.**

The root Gradle build, Core APIs, tests, CI, and IDE development baseline are Java 25. Minecraft backend processes are explicitly isolated and receive a selected Java executable from Runtime Manager.

## Why

The current primary target is Minecraft Java Edition 26.3. At the time of this ADR, 26.3 is still in the release-candidate stage upstream; this does not change the Java 25 toolchain decision. Minecraft 26.1 introduced a Java 25 requirement, and NeoForge's 26.1 migration documentation confirms the Java 21 → 25 transition. Gradle 9.8.0 supports Java 25 both for running Gradle and for Java toolchains.

Using Java 25 globally does **not** imply that historical Minecraft runtimes should be launched with Java 25. For example, NeoForge 1.21.1 requires JDK 21, Forge 1.20.1 requires JDK 17, and Forge 1.12.x requires JDK 8.

## Consequences

1. Core development is unambiguous: JDK 25+ is required.
2. Backend Java requirements are data, not global process state.
3. Runtime Manager must select an executable before launch.
4. A backend never invokes an ambient `java` command when a selected runtime is available.
5. A missing/wrong JDK becomes an explicit diagnostic.
6. Legacy backends remain separate processes and can keep their historical JDKs.

## Rejected alternatives

### One JDK for every Minecraft generation

Rejected because Minecraft/loader generations have different supported Java baselines. A single global JDK would couple legacy runtime compatibility to the Core build environment.

### Java 21 as the Core baseline

Rejected because the project's primary target is Minecraft 26.3 and Java 25 is required by the 26.x modernization path.

### Automatic JDK download as part of this milestone

Rejected for now. Runtime discovery and validation are implemented first. Any future installer must use allowlisted sources, checksum verification, architecture verification, and explicit installation metadata.
