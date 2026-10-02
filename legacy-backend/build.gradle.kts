plugins { id("unified-java-library") }

dependencies {
    api(project(":runtime-api"))
    api(project(":runtime-manager"))
    api(project(":version-api"))
    api(project(":compatibility-api"))
    testImplementation(project(":testkit"))
}
