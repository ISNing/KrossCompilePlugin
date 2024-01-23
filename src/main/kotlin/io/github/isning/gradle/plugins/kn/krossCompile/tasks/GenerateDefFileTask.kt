package io.github.isning.gradle.plugins.kn.krossCompile.tasks

import io.github.isning.gradle.plugins.kn.krossCompile.CInteropDefFileConfiguration
import io.github.isning.gradle.plugins.kn.krossCompile.CustomCInteropDefFileConfiguration
import io.github.isning.gradle.plugins.kn.krossCompile.content
import io.github.isning.gradle.plugins.kn.krossCompile.plus
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateDefFileTask : DefaultTask() {
    @get:Internal
    var configurations: List<CInteropDefFileConfiguration> = emptyList()

    @get:Input
    val configuration: CustomCInteropDefFileConfiguration
        get() = configurations.fold(
            CustomCInteropDefFileConfiguration()
        ) { acc, configuration -> acc + configuration }

    @get:OutputFile
    val outputFile: RegularFileProperty = project.objects.fileProperty()

    private val realOutputFile: File by lazy { outputFile.orNull?.asFile ?: error("Output file must be specified") }

    @TaskAction
    fun action() {
        realOutputFile.createNewFile()
        realOutputFile.writeText(configuration.content)
    }

    fun configureFrom(configuration: CInteropDefFileConfiguration) {
        configurations += configuration
    }

    fun configureFrom(configurations: List<CInteropDefFileConfiguration>) {
        this.configurations += configurations
    }
}
