# ADR 0005: Versioned typed TOML configuration

## Status

Accepted

## Decision

UnifiedMC Core uses a versioned TOML configuration schema with a typed Java model, explicit semantic validation, deterministic migrations, and secret-safe diagnostics.

The current schema is `unifiedmc-2`.

## Why TOML

TOML was already used by the repository and is intentionally chosen over YAML to reduce implicit typing and indentation-driven structure. Jackson TOML provides one parser/serializer boundary while the Java validator remains the authoritative semantic validation layer.

## Migration rule

Migrations are deterministic and in-memory by default. Validation does not rewrite operator configuration files. A future explicit `config migrate` command may persist migrated versions.

## Non-goals

The configuration layer does not implement mod compatibility, packet translation, backend lifecycle, or dependency download.
