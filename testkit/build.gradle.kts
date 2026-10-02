plugins { id("unified-java-library") }

dependencies {
    api(project(":api"))
    api(project(":runtime-api"))
    api(project(":version-api"))
}
