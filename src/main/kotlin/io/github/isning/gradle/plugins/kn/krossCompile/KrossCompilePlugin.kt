/*
 * Copyright 2024 ISNing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
