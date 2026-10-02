plugins { id("unified-java-library") }

dependencies {
    api(project(":mod-api"))
    api(project(":version-api"))
    api(project(":config"))
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.databind)
    implementation(libs.jackson.toml)
}
