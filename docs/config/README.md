# UnifiedMC configuration

The canonical server configuration is `config/unifiedmc.toml`. TOML is used intentionally because it is explicit, diff-friendly, and already established in the repository examples.

## Commands

```bash
unifiedmc config generate
unifiedmc config generate --force
unifiedmc config validate
unifiedmc config validate path/to/unifiedmc.toml
```

The current schema is `unifiedmc-2`. The configuration service accepts the previous `unifiedmc-1` directory-only skeleton and migrates it in memory. Persisting a migrated file is deliberately separate from validation so validation does not mutate operator configuration silently.

Validation diagnostics contain:

- source path;
- property path;
- expected value/type;
- actual value;
- possible fix.

Diagnostic values are redacted when the property name indicates a secret-bearing field.

The complete machine-readable schema is stored at `config/src/main/resources/schema/unifiedmc-2.schema.json`.

Configuration does not own mod compatibility logic. It only selects policy and capabilities; compatibility implementation remains in the dedicated compatibility modules.
