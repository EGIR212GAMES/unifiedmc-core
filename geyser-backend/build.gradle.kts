plugins { id("unified-java-library") }

dependencies {
    api(project(":content-api"))
    api(project(":content-ir"))
    api(project(":uapi"))
    testImplementation(project(":examples:uapi-adapter"))
}
