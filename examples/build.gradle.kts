plugins {
    base
}

// Aggregate lifecycle for nested example projects.
tasks.named("build") {
    dependsOn(":examples:uapi-adapter:build")
}

tasks.register("test") {
    group = "verification"
    description = "Runs tests for all example subprojects."
    dependsOn(":examples:uapi-adapter:test")
}

tasks.named("check") {
    dependsOn(":examples:uapi-adapter:check")
}
