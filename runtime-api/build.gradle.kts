plugins { id("unified-java-library") }

dependencies {
    api(project(":version-api"))
    api(platform(libs.jackson.bom))
    api(libs.jackson.databind)
}
