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

fun ModifiableCMakeTarget<*, *>.useKonan(target: String, executable: String = "run_konan") {
    configParams += (ModifiableCEntriesImpl().apply {
        compiler = "$executable;clang clang $target"
    } + ModifiableCXXEntriesImpl().apply {
        compiler = "$executable;clang clang++ $target"
    }).asCMakeParams
}