import dev.unifiedmc.buildlogic.VerifyArchitectureTask

val coreJavaSources = layout.projectDirectory.asFileTree.matching {
    include("**/src/main/java/**/*.java")
    exclude("**/build/**")
}

val verifyArchitecture = tasks.register<VerifyArchitectureTask>("verifyArchitecture") {
    group = "verification"
    description = "Checks that core modules remain free of Minecraft/loader implementation dependencies."
    sourceFiles.from(coreJavaSources)
}

tasks.named("check") {
    dependsOn(verifyArchitecture)
}
