# UnifiedMC Runtime Layer

The runtime layer treats each Minecraft generation/loader as an isolated process. The Core JVM does not share loader classpaths with backend processes.

## Runtime coordinate

Managed runtimes are identified by:

```text
<gameVersion>/<backend>/installation.json
```

The registry does not pin Minecraft/loader build numbers in its architectural catalog. An installation manifest must carry the exact artifact checksum, but the checksum is not itself a trust anchor: the RuntimeTrustStore must separately authorize the `(sourceId, sha256)` pair before a runtime can be started.

## Process model

```text
Core JVM (JDK 25)
        |
        | RuntimeRequest
        v
AbstractProcessRuntime
        |
        +--> selected JDK executable
        +--> isolated working directory
        +--> verified launch artifact
        +--> stdout/stderr capture
        +--> health
        +--> crash diagnostics
        v
Minecraft backend JVM
```

Legacy Forge runtimes remain separate JVM processes. Their Java runtime is selected from the runtime requirement rather than inherited from the Core JVM.

## Installation security

`ControlledRuntimeInstaller` intentionally performs no network download and no arbitrary artifact execution. Future official installers should implement `RuntimeInstaller` and must:

1. use allowlisted source identities;
2. verify a pinned SHA-256 before execution;
3. verify architecture and Java requirement;
4. write an installation manifest only after successful verification;
5. never execute an artifact directly from an untrusted download location.

NeoForge documents server installation through its official Maven distribution; UnifiedMC should eventually wrap that process in a backend-specific installer rather than embedding URL logic in Core. citeturn354184search7

Minecraft 26.1 officially requires Java 25, so the runtime metadata for 26.x is validated independently of the Core's own JDK. citeturn354184search0

## Legacy runtime boundary

`LEGACY_FORGE` is an explicit backend family. The experimental management roots are:

```text
legacy/1.7.10/
legacy/1.12.2/
```

The same `RuntimeRequest`/`AbstractProcessRuntime` process contract can host 1.18.2 and 1.20.1 Forge installations when a verified manifest exists. Legacy backend processes never share the Core classpath or a world directory with modern runtimes.

`LegacyCompatibilityAdapter` is a separate semantic/content boundary from `ProtocolAdapter`. Protocol translation does not imply mod compatibility, and legacy support does not imply automatic bytecode conversion.
