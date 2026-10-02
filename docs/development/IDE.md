# IDE setup

UnifiedMC Core development uses **JDK 25**. Configure the IDE project SDK and Gradle JVM to a JDK 25 installation.

The repository does not commit an IntelliJ `.idea` directory because an SDK identifier is machine-specific. `.vscode/settings.json` provides a portable Java 25 baseline for VS Code.

The Gradle Toolchain is authoritative for compilation and tests. A developer IDE may use a different JVM to bootstrap the Gradle client, but the Gradle daemon criteria and project toolchain must resolve Java 25.

For backend development, select the backend-specific JDK separately. Legacy backend processes are not part of the Core IDE JVM.
