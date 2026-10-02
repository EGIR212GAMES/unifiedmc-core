plugins { id("unified-java-library") }

dependencies {
    api(project(":uapi"))
    api(project(":content-api"))
    api(project(":mod-api"))
    api(project(":version-api"))
    testImplementation(project(":examples:uapi-adapter"))
}
