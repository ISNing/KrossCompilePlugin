package io.github.isning.gradle.plugins.kn.krossCompile.utils

import io.github.isning.gradle.plugins.cmake.ModifiableCMakeTarget
import io.github.isning.gradle.plugins.cmake.params.entries.asCMakeParams
import io.github.isning.gradle.plugins.cmake.params.entries.lang.ModifiableCEntriesImpl
import io.github.isning.gradle.plugins.cmake.params.entries.lang.ModifiableCXXEntriesImpl
import io.github.isning.gradle.plugins.cmake.params.entries.plus
import io.github.isning.gradle.plugins.cmake.params.plus
import org.gradle.api.Project

val Project.konanExecutable: String
    get() = (project.properties["konan.dir"] as String?)?.let {
        listOf(
            it,
            "bin",
            "run_konan"
        ).joinToString("/")
    } ?: findExecutableOnPath("run_konan") ?: error("run_konan executable not found in `konan.dir` or PATH")

fun parseKonanClangCommand(runKonanExecutable: String, tool: String, target: String): List<String> {
    val processBuilder = ProcessBuilder(runKonanExecutable, "clang", tool, target, "-###")
    val process = processBuilder.start()
    val output = process.inputStream.bufferedReader().readText()
    if (process.waitFor() != 0) {
        error("Failed to parse konan clang command: $output")
    }
    return output.lines().first().split(" ").filterNot { it == "-###" }
}

fun ModifiableCMakeTarget<*, *>.useKonan(target: String, executable: String = "run_konan", extraFlags: String? = null) {
    configParams += (ModifiableCEntriesImpl().apply {
        val parsed = parseKonanClangCommand(executable, "clang", target)
        compiler = parsed.first()
        flagsInit = parsed.drop(1).joinToString(" ")
        if (!extraFlags.isNullOrBlank()) {
            flagsInit += " $extraFlags"
        }
    } + ModifiableCXXEntriesImpl().apply {
        val parsed = parseKonanClangCommand(executable, "clang++", target)
        compiler = parsed.first()
        flagsInit = parsed.drop(1).joinToString(" ")
        if (!extraFlags.isNullOrBlank()) {
            flagsInit += " $extraFlags"
        }
    }).asCMakeParams
}