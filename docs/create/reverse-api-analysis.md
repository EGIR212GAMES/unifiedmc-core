# Create Compatibility Reverse/API Analysis

## Target selected

The first adapter targets the **stable Create 6.0.10 release for Minecraft 1.21.1 on NeoForge**. The source/API audit below was anchored to the immutable Git tag `mc1.21.1-6.0.10`. The `mc1.21.1/dev` branch was inspected separately only to understand the current development direction; its 6.0.11 metadata is not advertised as a released UnifiedMC target.

Sources:
- https://github.com/Creators-of-Create/Create/releases/tag/mc1.21.1-6.0.10
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/gradle.properties
- https://wiki.createmod.net/users/development-status
- https://wiki.createmod.net/developers/depend-on-create/neoforge-1.21.1

## Runtime dependencies observed from Create 1.21.1 source

The 6.0.10 release metadata pins Minecraft 1.21.1, NeoForge 21.1.219, Ponder 1.0.82, Flywheel 1.0.6 and Vanillin 1.1.3-41. The Create 1.21.1 mod metadata also declares Flywheel and Ponder as required dependencies. Optional integrations include Sodium, Lithium, JourneyMap and other ecosystem integrations. UnifiedMC treats Ponder/Flywheel as Create runtime dependencies but does not load either library into Core.

Source:
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/templates/META-INF/neoforge.mods.toml

## Registry usage

`AllBlocks` registers `create:shaft` and `create:cogwheel` using Registrate. The shaft is an axis block and the cogwheel is a small/large cog implementing the Create `IRotate`/`ICogWheel` contracts. `AllRecipeTypes` registers `create:pressing` as a processing recipe type.

Sources:
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/AllBlocks.java
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/AllRecipeTypes.java
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/kinetics/simpleRelays/CogWheelBlock.java

## Kinetic state and network

Create's `KineticBlockEntity` persists speed, source and a network payload containing network id, stress, capacity and network size. `KineticNetwork` tracks members/sources and calculates stress/capacity using speed-dependent multipliers. `RotationPropagator` discovers connected kinetic neighbours and calculates conveyed speed, including special gear/axis cases.

UnifiedMC does **not copy these classes**. The adapter implements a much smaller semantic model for the first vertical slice:

- axis-aware shaft links;
- explicit external mechanical power;
- deterministic network id;
- speed propagation;
- persistence of normalized state.

Sources:
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/kinetics/base/KineticBlockEntity.java
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/kinetics/KineticNetwork.java
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/kinetics/RotationPropagator.java
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/kinetics/simpleRelays/AbstractShaftBlock.java
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/kinetics/simpleRelays/CogWheelBlock.java

## Mechanical Press

The Create `MechanicalPressBlock` is a horizontal kinetic block with a `MechanicalPressBlockEntity`. Its block entity owns a `PressingBehaviour` and applies a `PressingRecipe` either to world items, belts or basins. `PressingBehaviour` persists running/mode/ticks state and drives the press cycle; the full implementation also contains client-side particles and Create sound events.

The UnifiedMC slice intentionally models only the server semantic path: a powered press + one deterministic pressing recipe. It does not reproduce belt/basin infrastructure, animation packets, particle effects or Create-specific client rendering.

Sources:
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/kinetics/press/MechanicalPressBlock.java
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/kinetics/press/MechanicalPressBlockEntity.java
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/kinetics/press/PressingBehaviour.java
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/kinetics/press/PressingRecipe.java
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/processing/recipe/ProcessingRecipeParams.java

## Contraptions

Create's `Contraption` is a substantial moving-structure abstraction. It searches/assembles a structure, serializes block and movement state, creates a collision world and owns client-side contraption representations. This is far beyond the first vertical slice and is marked `UNSUPPORTED` in the current Create profile.

Source:
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/contraptions/Contraption.java

## Rendering / Flywheel

Create 1.21.1 uses Flywheel for client-side rendering/visuals. Flywheel provides instancing and custom shader infrastructure. UnifiedMC therefore treats full Create visual parity as `CLIENT_ONLY`/not part of this server-side slice.

Sources:
- https://github.com/Engine-Room/Flywheel
- Create build metadata: https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/gradle.properties

## Networking

Create has its own network packets and synchronization for kinetic/contraption/client state. UnifiedMC does not reimplement those packet schemas. The semantic adapter is server-authoritative and exposes normalized state through UnifiedMC APIs.

## Resource packs and license

No Create assets are copied into UnifiedMC. Create's repository license states that code other than `src/main/resources/assets/` is MIT, while assets are All Rights Reserved. UnifiedMC therefore reuses concepts/API semantics and does not redistribute Create textures/models/sounds.

Source:
- https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/LICENSE.md
