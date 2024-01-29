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

import io.github.isning.gradle.plugins.cmake.CMakeConfiguration
import org.gradle.api.Action
import org.gradle.api.Project

interface KrossCompileConfiguration<T : CMakeConfiguration> {
    val cinterop: CInteropSettings
    val cmake: CMakeSettings<T>
    val sourceDir: String?

    /**
     * The relative path from the build directory to the output directory.
     */
    val outputPath: String?
    val libraryArtifactNames: List<String>?
}

interface ModifiableKrossCompileConfiguration<T : CMakeConfiguration> : KrossCompileConfiguration<T> {
    override val cinterop: ModifiableCInteropSettings
    override var sourceDir: String?
    override var outputPath: String?
    override var libraryArtifactNames: List<String>?
}

abstract class AbstractKrossCompileConfiguration<T : CMakeConfiguration>(project: Project) :
    ModifiableKrossCompileConfiguration<T> {
    override val cinterop: ModifiableCInteropSettings = CInteropSettingsImpl(project)
    override var sourceDir: String? = null
    override var outputPath: String? = null
    override var libraryArtifactNames: List<String>? = null
}

class KrossCompileConfigurationImpl<T : CMakeConfiguration>(
    override var sourceDir: String? = null,
    override var outputPath: String? = null,
    override var libraryArtifactNames: List<String>? = null,
    override var cinterop: CInteropSettings,
    override var cmake: CMakeSettings<T>
) : KrossCompileConfiguration<T> {
    constructor(sourceDir: String?, outputPath: String?, libraryArtifactNames: List<String>?, cinterop: CInteropSettings, cmake: CMakeSettings<T>, configure: KrossCompileConfiguration<T>.() -> Unit) : this(
        sourceDir,
        outputPath,
        libraryArtifactNames,
        cinterop,
        cmake) {
        configure()
    }

    constructor(origin: KrossCompileConfiguration<T>) : this(
        origin.sourceDir,
        origin.outputPath,
        origin.libraryArtifactNames,
        origin.cinterop,
        origin.cmake)

    constructor(origin: KrossCompileConfiguration<T>, configure: KrossCompileConfiguration<T>.() -> Unit) : this(origin) {
        configure()
    }
}

fun <T: CMakeConfiguration> KrossCompileConfiguration<T>.toModifiable(configure: KrossCompileConfigurationImpl<T>.() -> Unit = {}): KrossCompileConfigurationImpl<T> = KrossCompileConfigurationImpl(this).apply(configure)