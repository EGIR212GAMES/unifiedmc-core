plugins { id("unified-java-library") }

dependencies {
    api(platform(libs.jackson.bom))
    api(libs.jackson.annotations)
    implementation(libs.jackson.toml)
}
