# Architecture

UnifiedMC Core is a control plane over isolated Minecraft backend processes.

```text
Clients -> Protocol/Bedrock Edge -> UnifiedMC Core -> isolated backend JVM
```

Core must not import `net.minecraft`, `net.minecraftforge`, `net.neoforged`, or `net.fabricmc` implementation classes.
