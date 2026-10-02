# ADR 0007 — Capability-Based Universal Mod API

## Status

Accepted

## Context

UnifiedMC must avoid forcing a mod author to maintain independent Fabric, Forge, NeoForge and
Bedrock implementations for every supported runtime. At the same time, the project must not
pretend that Java and Bedrock runtime semantics are interchangeable.

## Decision

Define a backend-neutral `uapi` module containing semantic content contracts and explicit
adapter compatibility declarations.

Adapters expose capabilities, supported Minecraft versions/loaders, server-side support,
Polymer-like representation support, Bedrock representation support, custom Java client
requirements and documented limitations.

Java, server-side and Bedrock representations are distinct typed contracts. No representation
object is allowed to depend on implementation classes from another target.

## Consequences

- Content can be normalized once and projected into multiple targets.
- Partial compatibility is visible and machine-readable.
- Runtime-specific adapters remain outside Core.
- Unsupported semantics must be represented explicitly instead of silently approximated.
- Create and other specific compatibility adapters can be added later without changing UAPI.
