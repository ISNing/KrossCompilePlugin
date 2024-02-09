package io.github.isning.gradle.plugins.kn.krossCompile.utils

import io.github.isning.gradle.plugins.cmake.ModifiableCMakeTarget
import io.github.isning.gradle.plugins.cmake.params.entries.asCMakeParams
import io.github.isning.gradle.plugins.cmake.params.entries.lang.ModifiableCEntriesImpl
import io.github.isning.gradle.plugins.cmake.params.entries.lang.ModifiableCXXEntriesImpl
import io.github.isning.gradle.plugins.cmake.params.entries.plus
import io.github.isning.gradle.plugins.cmake.params.plus

fun ModifiableCMakeTarget<*, *>.useKonan(target: String, executable: String = "run_konan") {
    configParams += (ModifiableCEntriesImpl().apply {
        compiler = "$executable;clang clang $target"
    } + ModifiableCXXEntriesImpl().apply {
        compiler = "$executable;clang clang++ $target"
    }).asCMakeParams
}