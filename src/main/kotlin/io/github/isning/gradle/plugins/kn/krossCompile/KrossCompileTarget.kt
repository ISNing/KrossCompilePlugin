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
import io.github.isning.gradle.plugins.cmake.params.*
import io.github.isning.gradle.plugins.cmake.params.platform.*
import io.github.isning.gradle.plugins.kn.krossCompile.tasks.GenerateDefFileTask
import io.github.isning.gradle.plugins.kn.krossCompile.utils.upperCamelCaseName
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.internal.Factory
import org.gradle.kotlin.dsl.assign
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import kotlin.io.path.Path

interface KrossCompileTarget<T : CMakeTargetSettings<*>> : Named, KrossCompileConfiguration<T> {
    val targetName: String
    override val cinterop: CInteropSettings
    override val cmake: T
}

abstract class AbstractKrossCompileTarget<T : CMakeTargetSettings<*>>(project: Project) : KrossCompileTarget<T>,
    AbstractKrossCompileConfiguration<T>(project) {
    final override fun getName(): String = targetName

}

fun interface ModifiableCMakeTargetFactory<C : ModifiableCMakeGeneralParams, B : ModifiableCMakeBuildParams> {
    operator fun invoke(
        project: Project,
        targetName: String,
        cleanConfigParamsFactory: Factory<C>,
        cleanBuildParamsFactory: Factory<B>,
        buildParamsInitialOverlay: () -> CMakeParams?,
        configParamsInitialOverlay: () -> CMakeParams?,
    ): ModifiableCMakeTarget<C, B>
}

class KrossCompileTargetImpl<C : ModifiableCMakeGeneralParams, B : ModifiableCMakeBuildParams>(
    private val project: Project,
    override val targetName: String,
    rawTarget: AbstractCMakeTarget<C, B>,
    private val inheritedParents: List<KrossCompileConfiguration<*>>,
    private val inheritedNames: List<String>,
    buildParamsInitialOverlay: CMakeParams? = null,
    configParamsInitialOverlay: CMakeParams? = null,
) : AbstractKrossCompileTarget<ModifiableCMakeTargetSettings<ModifiableCMakeTarget<C, B>, C, B>>(project) {
    private val delegatedTarget: ModifiableCMakeTarget<C, B> = rawTarget.delegateWith(
        { buildParamsInitialOverlay },
        {
            sourceDir?.let {
                ModifiableCMakeGeneralParamsImpl().apply {
                    sourceDir = it
                } + configParamsInitialOverlay.orEmpty
            } ?: configParamsInitialOverlay
        },
    )

    override val cmake: ModifiableCMakeTargetSettings<ModifiableCMakeTarget<C, B>, C, B> =
        CMakeTargetSettingsImpl(project, this.delegatedTarget)

    private val defFileOutput: RegularFile = project.krossCompileDirectory.file(
        (listOf("def") + inheritedNames + targetName).joinToString(
            "/",
            postfix = ".def"
        )
    )

    private val knCInterop =
        project.kmpExtension.targets.named(targetName, KotlinNativeTarget::class.java).get()
            .compilations.named("main").get()
            .cinterops.maybeCreate(inheritedNames.last()).apply {
                defFile = defFileOutput.asFile //TODO: Is this working?
            }

    private val genDefFileTask = run {
        val configurations = inheritedParents + this@KrossCompileTargetImpl
        val taskNameSuffix = upperCamelCaseName(*(inheritedNames + targetName).toTypedArray())

        return@run project.tasks.register(
            TASK_NAME_PREFIX_GENERATE_DEF_FILE + taskNameSuffix, GenerateDefFileTask::class.java
        ) {
            val delegatedConfigurations = inheritedParents + object :
                KrossCompileConfiguration<ModifiableCMakeTargetSettings<ModifiableCMakeTarget<C, B>, C, B>> by this@KrossCompileTargetImpl {
                override val cinterop = object : CInteropSettings by this@KrossCompileTargetImpl.cinterop {
                    override val libraryPaths
                        get() = this@KrossCompileTargetImpl.cinterop.libraryPaths +
                                Path(
                                    this@KrossCompileTargetImpl.cmake.finalConfiguration.buildParams?.buildDirForBuild
                                        ?: error("buildDir is not set")
                                ).resolve(configurations.mapNotNull {
                                    it.outputPath
                                }.last()).toAbsolutePath().toString()
                    override val staticLibraries: List<String>
                        get() = this@KrossCompileTargetImpl.cinterop.staticLibraries +
                                configurations.mapNotNull {
                                    it.libraryArtifactNames
                                }.last()
                }
            }

            configureFrom(delegatedConfigurations.map { it.cinterop })

            outputFile = this@KrossCompileTargetImpl.defFileOutput
            group = "other"
            description = "Generate CInterop def file for $taskNameSuffix target"
        }
    }

    init {
        project.tasks.named(knCInterop.interopProcessingTaskName, CInteropProcess::class.java) {
            dependsOn(genDefFileTask)
            dependsOn(rawTarget.buildTask)
        }
    }
}

typealias KCGenericTarget = KrossCompileTargetImpl<ModifiableGenericParamsImpl, ModifiableCMakeBuildParamsImpl>
typealias KCHostTarget = KrossCompileTargetImpl<ModifiableHostParamsImpl, ModifiableCMakeBuildParamsImpl>
typealias KCAndroidTarget = KrossCompileTargetImpl<ModifiableAndroidParamsImpl, ModifiableCMakeBuildParamsImpl>
typealias KCIOSTarget = KrossCompileTargetImpl<ModifiableIOSParamsImpl, ModifiableCMakeBuildParamsImpl>
typealias KCIOSSimulatorTarget = KCIOSTarget
typealias KCWatchOSTarget = KrossCompileTargetImpl<ModifiableWatchOSParamsImpl, ModifiableCMakeBuildParamsImpl>
typealias KCWatchOSSimulatorTarget = KCWatchOSTarget
typealias KCTvOSTarget = KrossCompileTargetImpl<ModifiableTvOSParamsImpl, ModifiableCMakeBuildParamsImpl>
typealias KCTvOSSimulatorTarget = KCTvOSTarget
typealias KCLinuxTarget = KrossCompileTargetImpl<ModifiableLinuxParamsImpl, ModifiableCMakeBuildParamsImpl>
typealias KCMSVCTarget = KrossCompileTargetImpl<ModifiableMSVCParamsImpl, ModifiableCMakeBuildParamsImpl>
typealias KCMinGWTarget = KrossCompileTargetImpl<ModifiableMinGWParamsImpl, ModifiableCMakeBuildParamsImpl>
typealias KCDarwinTarget = KrossCompileTargetImpl<ModifiableDarwinParamsImpl, ModifiableCMakeBuildParamsImpl>
