# ADR 0010 — Legacy Runtime Isolation

## Decision

Legacy Minecraft generations run as separate JVM/backend processes. The Core control plane never loads legacy Forge classes into the modern Core JVM and never shares a legacy world directory with another generation.

## Rationale

Forge documents substantial differences between generations and historical Java requirements. The runtime abstraction already supports selecting a different Java executable per process, so legacy environments must remain isolated rather than being forced into the modern NeoForge classpath.

## Non-goals

- No arbitrary Forge 1.7.10 → modern bytecode conversion.
- No automatic world merging across Minecraft generations.
- No assumption that protocol translation implies mod compatibility.
