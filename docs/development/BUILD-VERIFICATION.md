# Build verification record

## Required verification

```bash
./gradlew clean
./gradlew build
./gradlew test
./gradlew dependencies
./gradlew javaToolchains
```

The primary build JVM and Java toolchain must be Java 25. `verifyCoreJdk` fails the build if the Gradle daemon is running under another major version.

The sandbox used to assemble this repository currently contains Java 21 but not Java 25 or a working Gradle installation. The existing wrapper files are a repository bootstrap shim rather than a downloaded Gradle distribution wrapper, and direct access to the Gradle distribution service is unavailable in this execution environment.

What can be checked locally in the sandbox is source-level compilation with the installed JDK 21 for syntax/API validation only. That is **not** equivalent to the required Java 25 Gradle build and is not reported as a successful project build.

CI provisions Gradle 9.8.0 and Temurin 25 and runs the real multi-project build/test pipeline.

## Audit result (2026-09-30)

The required commands were attempted in the build sandbox:

```text
./gradlew clean  -> blocked: repository bootstrap shim found no Gradle 9.8.0 installation
./gradlew build  -> blocked: repository bootstrap shim found no Gradle 9.8.0 installation
./gradlew test   -> blocked: repository bootstrap shim found no Gradle 9.8.0 installation
```

Source-level production compilation with the installed JDK 21 passed with `javac --release 21 -Xlint:all -Werror`. This validates Java source syntax/API usage only; it does not validate the required Java 25 Gradle toolchain.

CI remains the authoritative build environment for this repository until a machine with JDK 25 and Gradle 9.8.0 is available.
