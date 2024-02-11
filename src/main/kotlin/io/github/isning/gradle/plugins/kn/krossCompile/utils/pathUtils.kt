package io.github.isning.gradle.plugins.kn.krossCompile.utils

import java.io.File

fun findExecutableOnPath(name: String): String? {
    for (dirname in System.getenv("PATH").split(File.pathSeparator)) {
        val file = File(dirname, name)
        if (file.isFile() && file.canExecute()) {
            return file.absolutePath
        }
    }
    return null
}