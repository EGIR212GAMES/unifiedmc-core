plugins { id("unified-java-library") }

dependencies {
    api(project(":api"))
    api(project(":runtime-api"))
    api(project(":mod-api"))
    implementation(project(":runtime-manager"))
    implementation(project(":config"))
    implementation(project(":mod-manager"))
    implementation(project(":dependency-resolver"))
    implementation(project(":compatibility-api"))
    implementation(project(":version-api"))
}
