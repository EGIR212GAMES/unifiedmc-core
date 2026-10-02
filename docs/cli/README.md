# UnifiedMC CLI

The CLI is the foreground control-plane entry point. It never loads Minecraft classes into the Core JVM.

## Lifecycle

`unifiedmc start` executes the phases:

```text
BOOTSTRAP
CONFIG_LOAD
ENVIRONMENT_CHECK
RUNTIME_DISCOVERY
MOD_DISCOVERY
DEPENDENCY_RESOLUTION
COMPATIBILITY_ANALYSIS
BACKEND_SELECTION
RESOURCE_PREPARATION
SERVER_START
READY
```

Every phase emits typed `LifecycleEvent` objects with STARTED/COMPLETED/FAILED transitions. Failures are classified with `FailureClass`.

## Commands

```text
unifiedmc start [config]
unifiedmc stop [config]
unifiedmc restart [config]
unifiedmc status [config]
unifiedmc doctor
unifiedmc config validate [config]
unifiedmc config generate [config] [--force]
unifiedmc mods list [config]
unifiedmc mods scan [config]
unifiedmc versions list
unifiedmc runtimes list
unifiedmc runtimes doctor
unifiedmc runtimes install <version> <backend>
```

`start` is a foreground process. Ctrl-C/termination causes the JVM shutdown hook to invoke graceful shutdown. `stop` uses the persisted PID to send a graceful process termination request.

## Current scope

The orchestration layer deliberately does not implement concrete Minecraft server processes, loader compatibility, protocol translation, or resource-pack compilation. Unsupported stages return explicit failures instead of falling back silently.

## Runtime installation policy

`unifiedmc runtimes install <version> <backend>` is intentionally controlled/stubbed in the current milestone. It does not download from arbitrary URLs and does not execute an artifact until a trusted backend installer and checksum trust store are registered.
