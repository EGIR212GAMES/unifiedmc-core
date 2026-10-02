plugins { id("unified-java-library") }

dependencies {
    api(project(":runtime-api"))
    api(project(":version-api"))
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.databind)
    testImplementation(project(":testkit"))
}
