package io.github.isning.gradle.plugins.kn.krossCompile

import io.github.isning.gradle.plugins.cmake.CMakePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

const val KROSS_COMPILE_PROJECT_EXTENSION_NAME = "krossCompile"
const val TASK_NAME_PREFIX_GENERATE_DEF_FILE = "generateDefFile"

class KrossCompilePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(CMakePlugin::class.java)

        project.extensions.create(KROSS_COMPILE_PROJECT_EXTENSION_NAME, KrossCompileExtension::class.java, project)
    }
}
