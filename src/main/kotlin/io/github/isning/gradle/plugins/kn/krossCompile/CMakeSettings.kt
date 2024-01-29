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

import io.github.isning.gradle.plugins.cmake.*
import io.github.isning.gradle.plugins.cmake.params.ModifiableCMakeBuildParams
import io.github.isning.gradle.plugins.cmake.params.ModifiableCMakeGeneralParams
import io.github.isning.gradle.plugins.cmake.utils.Delegated
import org.gradle.api.Project

interface CMakeSettings<T : CMakeConfiguration> : CMakeConfiguration, Delegated<T> {
    override val delegate: T
}
interface ModifiableCMakeSettings<C : ModifiableCMakeGeneralParams, B : ModifiableCMakeBuildParams> :
    CMakeSettings<ModifiableCMakeConfiguration<C, B>>, ModifiableCMakeConfiguration<C, B>

interface CMakeLibrarySettings<T : CMakeProject> : CMakeSettings<T>, CMakeProject

interface ModifiableCMakeLibrarySettings<T :ModifiableCMakeProject> : CMakeLibrarySettings<T>, ModifiableCMakeProject

interface CMakeTargetSettings<T : CMakeTarget> : CMakeSettings<T>, CMakeTarget
interface ModifiableCMakeTargetSettings<T: ModifiableCMakeTarget<C, B>, C : ModifiableCMakeGeneralParams, B : ModifiableCMakeBuildParams> :
    CMakeTargetSettings<T>, ModifiableCMakeTarget<C, B>

operator fun <T : CMakeSettings<*>> T.invoke(configure: T.() -> Unit): Unit = configure()

class CMakeSettingsImpl<C : ModifiableCMakeGeneralParams, B : ModifiableCMakeBuildParams>(
    val project: Project,
    override val delegate: ModifiableCMakeConfiguration<C, B>
) : ModifiableCMakeSettings<C, B>, ModifiableCMakeConfiguration<C, B> by delegate

class CMakeLibrarySettingsImpl<T :ModifiableCMakeProject>(
    val project: Project,
    override val delegate: T
) : ModifiableCMakeLibrarySettings<T>, ModifiableCMakeProject by delegate {
    @ManagedByKrossCompile
    override val rawTargets: MutableSet<CMakeTarget>
        get() = delegate.rawTargets
}

class CMakeTargetSettingsImpl<T: ModifiableCMakeTarget<C, B>, C : ModifiableCMakeGeneralParams, B : ModifiableCMakeBuildParams>(
    val project: Project,
    override val delegate: T
) : ModifiableCMakeTargetSettings<T, C, B>, ModifiableCMakeTarget<C, B> by delegate
