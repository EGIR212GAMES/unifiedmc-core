# Java Runtime Compatibility Matrix

> Minecraft 26.3 is the current primary UnifiedMC target. Upstream 26.3 is still in release-candidate status as of this audit; the Java 25 baseline remains the 26.x runtime decision established by 26.1.

This matrix separates the Java required by UnifiedMC Core from the Java used by an individual Minecraft backend process.

| Minecraft generation | Loader/backend baseline | Required Java | Verification status | Evidence |
|---|---|---:|---|---|
| 1.7.10 | Forge legacy | **Not pinned yet** | UNVERIFIED | A version-specific authoritative Forge prerequisite page was not found during this audit; UnifiedMC will not guess. |
| 1.12.2 | Forge | 8 | CONFIRMED | Forge 1.12.x prerequisites require JDK 8. |
| 1.18.2 | Forge | 17 | CONFIRMED | ForgeGradle documentation lists 1.18–1.19 as JDK 17. |
| 1.20.1 | Forge | 17 | CONFIRMED | Forge 1.20.1 prerequisites require JDK 17. |
| 1.20.2 | NeoForge | 17 | CONFIRMED | NeoForge user guide: 1.20.2–1.20.4 use Java 17. |
| 1.20.3 | NeoForge | 17 | CONFIRMED | NeoForge user guide: 1.20.2–1.20.4 use Java 17. |
| 1.20.4 | NeoForge | 17 | CONFIRMED | NeoForge user guide: 1.20.2–1.20.4 use Java 17. |
| 1.20.5 | Modern | 21 | CONFIRMED | Minecraft 1.20.5 explicitly requires Java 21. |
| 1.20.6 | Modern | 21 | CONFIRMED | NeoForge user guide states 1.20.5-latest uses Java 21. |
| 1.21.1 | NeoForge | 21 | CONFIRMED | NeoForge 1.21.1 getting-started prerequisites require JDK 21. |
| 26.1 | NeoForge/modern | 25 | CONFIRMED | Minecraft 26.1 explicitly requires Java 25; NeoForge migration primer says JDK upgraded 21 → 25. |
| 26.2 | NeoForge/modern | 25 | INHERITED 26.X BASELINE | Java 25 is established for 26.1. The official NeoForge 26.1.x → 26.2 migration path does not document a Java requirement change; keep the 26.x baseline at 25 until upstream states otherwise. |
| 26.3 | NeoForge/modern | 25 | INHERITED 26.X BASELINE | The official NeoForge 26.2 → 26.3 migration path does not document a Java requirement change; Java 25 remains the 26.x baseline established by 26.1. |

## Policy

When `minimumJava == recommendedJava`, the catalog treats that value as a pinned runtime requirement. A backend will not silently substitute a newer Java major. A broader range can only be represented explicitly by setting `recommendedJava` above `minimumJava`.


- Core build/test JDK: **25**.
- Backend launch is process-isolated.
- Runtime Manager selects a concrete `java` executable for every backend.
- A missing or unverified JDK is reported as a diagnostic; there is no fallback to the global `java` command.
- Auto-installation of JDKs is not implemented.
- 1.7.10 remains intentionally unpinned until an authoritative version-specific runtime requirement is verified.

## Primary sources

- Minecraft 26.1: https://www.minecraft.net/en-us/article/minecraft-java-edition-26-1
- Minecraft 1.20.5: https://www.minecraft.net/en-us/article/minecraft-java-edition-1-20-5
- NeoForge Java guide: https://docs.neoforged.net/user/docs/
- NeoForge 1.21.1 getting started: https://docs.neoforged.net/docs/1.21.1/gettingstarted/
- NeoForge 26.1 primer: https://docs.neoforged.net/primer/docs/26.1/
- NeoForge 26.3 primer: https://docs.neoforged.net/primer/docs/26.3/
- Forge 1.12.x getting started: https://docs.minecraftforge.net/en/1.12.x/gettingstarted/
- ForgeGradle Java matrix: https://docs.minecraftforge.net/en/fg-5.x/gettingstarted/
- Forge 1.20.1 getting started: https://docs.minecraftforge.net/en/1.20.1/gettingstarted/
