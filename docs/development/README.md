# Development

## Required JDK

Use **JDK 25+** for UnifiedMC Core development. The repository pins its Gradle daemon criteria and Java toolchain to 25, and CI builds on Temurin 25.

Gradle 9.8.0 supports running Gradle on Java 25 and supports Java 25 toolchains. The project also disables automatic JDK downloading: missing toolchains must be installed through an operator-controlled environment. ([Gradle compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html), [Gradle toolchains](https://docs.gradle.org/current/userguide/toolchains.html))

Recommended IDE baseline for 26.1+ work is an IDE version with Java 25 support; NeoForge's 26.1 migration primer calls this out explicitly.

```bash
./gradlew clean build
./gradlew test
./gradlew spotlessApply
./gradlew javaToolchains
./gradlew verifyCoreJdk
```

The project uses `.java-version` and `.sdkmanrc` as developer hints. They do not download or install a JDK.

When dependencies are changed, review and update Gradle dependency verification metadata with a trusted `--write-verification-metadata sha256` run.
