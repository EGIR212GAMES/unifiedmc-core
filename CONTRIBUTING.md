# Contributing

Run `./gradlew clean check` before opening a PR.

Keep Core modules free of Minecraft/loader implementation dependencies. New runtime-specific code belongs behind a backend boundary.

Do not add a generic bytecode translator or silent fallback. Any new compatibility behavior must have an explicit capability, version scope, tests, and a documented failure mode.
