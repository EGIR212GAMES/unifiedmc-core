# Legacy Backend Architecture

Legacy support means **running a legacy environment, unless a dedicated compatibility adapter exists**.

UnifiedMC Core owns the control plane: player identity, permissions, chat, discovery, routing, global configuration and capability negotiation. The legacy backend owns its own Minecraft process, Forge loader, world and mod lifecycle.

## Runtime boundary

```text
Unified Core
    │
    ▼
Backend Orchestrator
    │
    ▼
Legacy Runtime
    │
    ▼
Minecraft / Forge process
```

Legacy runtimes never share a JVM/classpath with modern NeoForge/Fabric runtimes.

## Supported bridge modes

| Mode | Meaning | Limitations |
|---|---|---|
| `ISOLATED_WORLD` | Legacy world is independent | No live world state sharing |
| `SHARED_PLAYER` | Core identity/permissions/chat are shared | Player state remains backend-specific |
| `PORTAL_TRANSFER` | Player is explicitly routed between backend worlds | Requires gateway/session handoff; no live world merge |
| `MIRRORED_DATA` | Selected data is copied between worlds | Requires explicit schema/migration rules |
| `TRANSLATED_CONTENT` | Known content mappings are translated | Requires a dedicated `LegacyCompatibilityAdapter`; never automatic bytecode conversion |

`ISOLATED_WORLD` is the default and safest mode.

## Crash recovery

A legacy process may fail without taking Core down. The supervisor marks the backend `FAILED`, retains stdout/stderr crash diagnostics, notifies the Core gateway through `LegacyBridge`, and may perform a bounded number of automatic restarts.

Players receive a controlled error through the gateway boundary. The legacy process is never treated as authoritative for Core lifecycle state.

## Protocol vs compatibility

`LegacyProtocolProfile` describes the protocol boundary only. It must not be used to infer mod compatibility. `LegacyCompatibilityAdapter` is the separate content/mod compatibility contract.

## Version baseline

Forge's official legacy documentation explicitly warns that older Minecraft generations have significant differences. Forge's published toolchain documentation lists JDK 8 for 1.12–1.16 and JDK 17 for 1.18–1.19. NeoForge 1.21.1 requires JDK 21. These constraints reinforce the process/JDK isolation model. 

References:
- https://docs.minecraftforge.net/en/1.20.1/legacy/
- https://docs.minecraftforge.net/en/fg-5.x/gettingstarted/
- https://docs.neoforged.net/docs/1.21.1/gettingstarted/
