plugins {
    `java-library`
    `maven-publish`
    checkstyle
    id("com.diffplug.spotless")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withSourcesJar()
}

checkstyle {
    toolVersion = "10.26.1"
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
}

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
        }
    }
}
