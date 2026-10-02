plugins {
    base
}

// Aggregate lifecycle for the nested :backend:api project.
tasks.named("build") {
    dependsOn(":backend:api:build")
}

tasks.register("test") {
    group = "verification"
    description = "Runs tests for all backend subprojects."
    dependsOn(":backend:api:test")
}

tasks.named("check") {
    dependsOn(":backend:api:check")
}
