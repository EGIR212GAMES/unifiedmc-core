package dev.unifiedmc.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class VerifyArchitectureTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @TaskAction
    fun verify() {
        val forbidden = setOf(
            "net.minecraft",
            "net.minecraftforge",
            "net.neoforged",
            "net.fabricmc"
        )
        val forbiddenHits = sourceFiles.files.flatMap { file ->
            file.readLines().mapIndexedNotNull { index, line ->
                if (forbidden.any(line::contains)) {
                    "${file.absolutePath}:${index + 1}: $line"
                } else {
                    null
                }
            }
        }
        if (forbiddenHits.isNotEmpty()) {
            throw GradleException(
                "Core source tree contains forbidden runtime imports:\n" +
                    forbiddenHits.joinToString("\n")
            )
        }
    }
}
