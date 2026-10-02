plugins {
    id("unified-java-library")
    application
}

application {
    mainClass.set("dev.unifiedmc.cli.Main")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":runtime-api"))
    implementation(project(":runtime-manager"))
    implementation(project(":version-api"))
    implementation(project(":mod-api"))
    implementation(project(":mod-manager"))
    implementation(project(":dependency-resolver"))
    implementation(project(":compatibility-api"))
    implementation(project(":compatibility-engine"))
    implementation(project(":legacy-backend"))
    implementation(project(":config"))
}
