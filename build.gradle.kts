import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

plugins {
    base
    alias(libs.plugins.spotless)
    id("unified-root-verification")
}

group = "dev.unifiedmc"
version = providers.gradleProperty("projectVersion").get()

subprojects {
    group = rootProject.group
    version = rootProject.version

    plugins.withId("java-library") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
            }
            withSourcesJar()
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(25)
            options.isIncremental = true
            options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
        }

        tasks.withType<Jar>().configureEach {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }

        val javaToolchains = project.extensions.getByType<JavaToolchainService>()
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(25))
            })
        }

        dependencies {
            "testImplementation"(platform(libs.junit.bom))
            "testImplementation"(libs.junit.jupiter)
            "testRuntimeOnly"(libs.junit.platform.launcher)
        }
    }
}

allprojects {
    plugins.withId("com.diffplug.spotless") {
        extensions.configure<SpotlessExtension> {
            // Formatting remains available via spotlessApply/spotlessCheck; existing source is not yet a formatting baseline.
            setEnforceCheck(false)
            java {
                target("src/**/*.java")
                targetExclude("src/main/java/dev/unifiedmc/cli/Main.java")
                if (project.path == ":compatibility-adapters:create") {
                    targetExclude("src/main/java/dev/unifiedmc/create/**/*.java")
                }
                googleJavaFormat("1.30.0").aosp()
                removeUnusedImports()
                trimTrailingWhitespace()
                endWithNewline()
            }
        }
    }
}

tasks.named("build") {
    dependsOn(subprojects.map { "${it.path}:build" })
    dependsOn("verifyCoreJdk", "verifyArchitecture")
}

tasks.register("test") {
    group = "verification"
    description = "Runs all project unit tests."
    dependsOn(subprojects.map { "${it.path}:test" })
}

tasks.register("verifyCoreJdk") {
    group = "verification"
    description = "Ensures the Gradle daemon is running on the UnifiedMC Core development JDK."
    doLast {
        val major = JavaVersion.current().majorVersion.toInt()
        if (major != 25) {
            throw GradleException(
                "UnifiedMC Core requires JDK 25 to run Gradle. Current Gradle JVM: Java $major"
            )
        }
    }
}

tasks.named("check") {
    group = "verification"
    description = "Runs verification for all subprojects and architecture rules."
    dependsOn(
        "verifyCoreJdk",
        subprojects.map { "${it.path}:check" }
    )
}
