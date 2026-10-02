plugins {
    base
}

// Aggregate lifecycle for nested compatibility adapter projects.
tasks.named("build") {
    dependsOn(":compatibility-adapters:create:build")
}

tasks.register("test") {
    group = "verification"
    description = "Runs tests for all compatibility adapter subprojects."
    dependsOn(":compatibility-adapters:create:test")
}

tasks.named("check") {
    dependsOn(":compatibility-adapters:create:check")
}
