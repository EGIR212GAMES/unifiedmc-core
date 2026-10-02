plugins { id("unified-java-library") }

dependencies {
    api(project(":uapi"))
    api(project(":content-api"))
    api(project(":content-ir"))
    api(project(":version-api"))
    testImplementation(project(":examples:uapi-adapter"))
}
