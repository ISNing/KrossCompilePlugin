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
