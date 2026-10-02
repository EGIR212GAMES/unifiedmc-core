# Legacy Runtime Management Status

The legacy directories in this repository are management skeletons, not bundled Minecraft/Forge distributions.

- `1.7.10` — manifest template only; exact Forge build and Java requirement must be operator-pinned and verified before installation.
- `1.12.2` — manifest template only; Forge documentation establishes JDK 8 for the 1.12–1.16 family.
- `1.18.2` / `1.20.1` — supported by the generic `LEGACY_FORGE` runtime abstraction, but require their own verified runtime manifests.

No legacy server jar, Forge binary, mod jar, or proprietary Minecraft asset is redistributed here.
