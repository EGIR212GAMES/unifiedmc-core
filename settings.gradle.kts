pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

plugins {
    id("com.diffplug.spotless") version "8.10.3" apply false
}

rootProject.name = "unifiedmc-core"

include(
    ":api",
    ":core",
    ":runtime-api",
    ":runtime-manager",
    ":mod-api",
    ":mod-manager",
    ":dependency-resolver",
    ":version-api",
    ":version-manager",
    ":protocol",
    ":compatibility-api",
    ":compatibility-engine",
    ":content-api",
    ":content-ir",
    ":uapi",
    ":polymer-backend",
    ":geyser-backend",
    ":resource-pack",
    ":security",
    ":config",
    ":cli",
    ":testkit",
    ":backend:api",
    ":examples:uapi-adapter",
    ":compatibility-adapters:create",
    ":legacy-backend",
)

// Concrete Minecraft/loader runtimes remain isolated from Core.
// legacy-backend provides only the control-plane/process abstraction; it does not embed Forge, Minecraft, or loader classes.
