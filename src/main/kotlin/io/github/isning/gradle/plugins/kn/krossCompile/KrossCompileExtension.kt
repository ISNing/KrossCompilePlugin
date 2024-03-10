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

import io.github.isning.gradle.plugins.cmake.ModifiableCMakeConfiguration
import io.github.isning.gradle.plugins.cmake.cmakeExtension
import io.github.isning.gradle.plugins.cmake.delegateWith
import io.github.isning.gradle.plugins.cmake.params.*
import io.github.isning.gradle.plugins.kn.krossCompile.utils.delegateItemsTransformedTo
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal val Project.krossCompileExtension: KrossCompileExtension
    get() = extensions.getByName(KROSS_COMPILE_PROJECT_EXTENSION_NAME) as KrossCompileExtension

internal val Project.kmpExtension: KotlinMultiplatformExtension
    get() = project.extensions.findByType(KotlinMultiplatformExtension::class.java)!!

internal val Project.krossCompileDirectory: Directory
    get() = project.layout.buildDirectory.dir("krossCompile").get()

open class KrossCompileExtension(project: Project) :
    AbstractKrossCompileConfiguration<ModifiableCMakeSettings<ModifiableCMakeGeneralParams, ModifiableCMakeBuildParams>>(
        project
    ) {
    override val cmake: ModifiableCMakeSettings<ModifiableCMakeGeneralParams, ModifiableCMakeBuildParams> =
        CMakeSettingsImpl(project, project.cmakeExtension.delegateWith({ null }, {
            sourceDir?.let {
                ModifiableCMakeGeneralParamsImpl().apply {
                    sourceDir = this@KrossCompileExtension.sourceDir
                }
            } }))

    val rawLibraries: NamedDomainObjectContainer<KrossCompileLibrary<*>> =
        project.container(KrossCompileLibrary::class.java).apply {
            delegateItemsTransformedTo(project.cmakeExtension.rawProjects) { it.cmake }
        }

    val libraries: NamedDomainObjectContainer<KrossCompileLibraryImpl> =
        project.container(KrossCompileLibraryImpl::class.java) {
            KrossCompileLibraryImpl(project, it, listOf(this), emptyList())
        }
}
