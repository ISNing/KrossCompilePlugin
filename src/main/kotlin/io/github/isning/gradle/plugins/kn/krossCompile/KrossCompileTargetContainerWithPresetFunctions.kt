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
import io.github.isning.gradle.plugins.cmake.params.CustomCMakeParams
import io.github.isning.gradle.plugins.cmake.params.ModifiableCMakeBuildParams
import io.github.isning.gradle.plugins.cmake.params.ModifiableCMakeGeneralParams
import io.github.isning.gradle.plugins.cmake.params.entries.CustomCMakeCacheEntries
import io.github.isning.gradle.plugins.cmake.params.entries.asCMakeParams
import io.github.isning.gradle.plugins.cmake.params.entries.platform.ModifiablePlatformEntriesImpl
import io.github.isning.gradle.plugins.cmake.params.platform.*
import io.github.isning.gradle.plugins.cmake.params.plus
import io.github.isning.gradle.plugins.cmake.targets.*
import io.github.isning.gradle.plugins.kn.krossCompile.utils.decamelizeWith
import io.github.isning.gradle.plugins.kn.krossCompile.utils.konanExecutable
import io.github.isning.gradle.plugins.kn.krossCompile.utils.useKonan
import org.gradle.api.Action
import org.gradle.api.Project

fun <T : KrossCompileTarget<*>> (Pair<String, (String) -> T?>).factory(configure: T.() -> Unit = {}): KrossCompileTargetFactory<T> =
    object : KrossCompileTargetFactory<T> {
        override val factoryName: String = first
        override fun create(name: String): T? = second.invoke(name)?.apply(configure)
    }

@Suppress("UNCHECKED_CAST", "UnusedReceiverParameter")
fun <T : AbstractCMakeTarget<C, B>, C : ModifiableCMakeGeneralParams, B : ModifiableCMakeBuildParams> KrossCompileTargetContainerWithFactoriesRegisterer.defaultFactory(
    project: Project,
    container: CMakeTargetContainerWithFactories,
    name: String,
    inheritedParents: List<KrossCompileConfiguration<*>>,
    inheritedNames: List<String>,
    cmakeFactoryName: String = name,
    configure: KrossCompileTargetImpl<C, B>.() -> Unit = {}
): KrossCompileTargetFactory<KrossCompileTargetImpl<C, B>> = (name to { targetName: String ->
    KrossCompileTargetImpl(
        project,
        targetName,
        container.factories.named(cmakeFactoryName).get().create(targetName) as T,
        inheritedParents,
        inheritedNames
    )
}).factory(configure)

interface KrossCompileTargetContainerWithFactoriesRegisterer : KrossCompileTargetContainerWithPresetFunctions {
    fun registerFactories(
        project: Project,
        container: CMakeTargetContainerWithPresetFunctions,
        inheritedParents: List<KrossCompileConfiguration<*>>,
        inheritedNames: List<String>
    ) {
        fun KrossCompileTarget<out ModifiableCMakeTargetSettings<*, *, *>>.useKonan(target: String, extraFlags: String? = null) {
            cmake {
                useKonan(
                    target.decamelizeWith("_"),
                    project.konanExecutable,
                    extraFlags
                )
            }
        }
        fun KrossCompileTarget<out ModifiableCMakeTargetSettings<*, *, *>>.useKonanAndLld(target: String, extraFlags: String? = null) {
            useKonan(
                target,
                extraFlags
            )
            cmake {
                // Wait for https://youtrack.jetbrains.com/issue/KT-56569 to use lld from konan
                forceUseLld()
            }
        }

        factories.add(defaultFactory<HostTarget, _, _>(project, container, "host", inheritedParents, inheritedNames))

        factories.add(
            defaultFactory<AndroidTarget, _, _>(
                project,
                container,
                "android",
                inheritedParents,
                inheritedNames
            )
        )
        listOf(
            "androidX64",
            "androidX86",
            "androidArm32",
            "androidArm64",
        ).forEach {
            listOf("ndk", "clang", "zig").forEach { variant ->
                factories.add(
                    defaultFactory<AndroidTarget, _, _>(
                        project,
                        container,
                        "$it.$variant",
                        inheritedParents,
                        inheritedNames,
                    )
                )
            }
            factories.add(
                defaultFactory<AndroidTarget, _, _>(
                    project,
                    container,
                    "$it.konan",
                    inheritedParents,
                    inheritedNames,
                    "android"
                ) {
                    useKonanAndLld(it)
                }
            )
        }

        factories.add(defaultFactory<IOSTarget, _, _>(project, container, "ios", inheritedParents, inheritedNames))
        listOf(
            "iosArm64",
        ).forEach {
            listOf("xcode", "clang", "zig").forEach { variant ->
                factories.add(
                    defaultFactory<IOSTarget, _, _>(
                        project,
                        container,
                        "$it.$variant",
                        inheritedParents,
                        inheritedNames
                    )
                )
            }
            factories.add(
                defaultFactory<IOSTarget, _, _>(
                    project,
                    container,
                    "$it.konan",
                    inheritedParents,
                    inheritedNames,
                    "ios"
                ) {
                    useKonan(it)
                }
            )
        }

        factories.add(
            defaultFactory<IOSSimulatorTarget, _, _>(
                project,
                container,
                "iosSimulator",
                inheritedParents,
                inheritedNames
            )
        )
        listOf(
            "iosSimulatorArm64",
            "iosSimulatorX64",
        ).forEach {
            listOf("xcode", "clang", "zig").forEach { variant ->
                factories.add(
                    defaultFactory<IOSSimulatorTarget, _, _>(
                        project,
                        container,
                        "$it.$variant",
                        inheritedParents,
                        inheritedNames
                    )
                )
            }
            factories.add(
                defaultFactory<IOSSimulatorTarget, _, _>(
                    project,
                    container,
                    "$it.konan",
                    inheritedParents,
                    inheritedNames,
                    "iosSimulator"
                ) {
                    useKonan(it.replace("SimulatorX64$".toRegex(), "X64"))
                }
            )
        }

        factories.add(
            defaultFactory<WatchOSTarget, _, _>(
                project,
                container,
                "watchos",
                inheritedParents,
                inheritedNames
            )
        )
        listOf(
            "watchosArm32",
            "watchosArm64",
        ).forEach {
            listOf("xcode", "clang", "zig").forEach { variant ->
                factories.add(
                    defaultFactory<WatchOSTarget, _, _>(
                        project,
                        container,
                        "$it.$variant",
                        inheritedParents,
                        inheritedNames
                    )
                )
            }
            factories.add(
                defaultFactory<WatchOSTarget, _, _>(
                    project,
                    container,
                    "$it.konan",
                    inheritedParents,
                    inheritedNames,
                    "watchos"
                ) {
                    useKonan(it)
                }
            )
        }

        factories.add(
            defaultFactory<WatchOSSimulatorTarget, _, _>(
                project,
                container,
                "watchosSimulator",
                inheritedParents,
                inheritedNames
            )
        )
        listOf(
            "watchosSimulatorArm64",
            "watchosSimulatorX64",
        ).forEach {
            listOf("xcode", "clang", "zig").forEach { variant ->
                factories.add(
                    defaultFactory<WatchOSSimulatorTarget, _, _>(
                        project,
                        container,
                        "$it.$variant",
                        inheritedParents,
                        inheritedNames
                    )
                )
            }
            factories.add(
                defaultFactory<WatchOSSimulatorTarget, _, _>(
                    project,
                    container,
                    "$it.konan",
                    inheritedParents,
                    inheritedNames,
                    "watchosSimulator"
                ) {
                    useKonan(it.replace("SimulatorX64$".toRegex(), "X64"))
                }
            )
        }

        factories.add(defaultFactory<TvOSTarget, _, _>(project, container, "tvos", inheritedParents, inheritedNames))
        listOf(
            "tvosArm64",
        ).forEach {
            listOf("xcode", "clang", "zig").forEach { variant ->
                factories.add(
                    defaultFactory<TvOSTarget, _, _>(
                        project,
                        container,
                        "$it.$variant",
                        inheritedParents,
                        inheritedNames
                    )
                )
            }
            factories.add(
                defaultFactory<TvOSTarget, _, _>(
                    project,
                    container,
                    "$it.konan",
                    inheritedParents,
                    inheritedNames,
                    "tvos"
                ) {
                    useKonan(it)
                }
            )
        }

        factories.add(
            defaultFactory<TvOSSimulatorTarget, _, _>(
                project,
                container,
                "tvosSimulator",
                inheritedParents,
                inheritedNames
            )
        )
        listOf(
            "tvosSimulatorArm64",
            "tvosSimulatorX64",
        ).forEach {
            listOf("xcode", "clang", "zig").forEach { variant ->
                factories.add(
                    defaultFactory<TvOSSimulatorTarget, _, _>(
                        project,
                        container,
                        "$it.$variant",
                        inheritedParents,
                        inheritedNames
                    )
                )
            }
            factories.add(
                defaultFactory<TvOSSimulatorTarget, _, _>(
                    project,
                    container,
                    "$it.konan",
                    inheritedParents,
                    inheritedNames,
                    "tvosSimulator"
                ) {
                    useKonan(it.replace("SimulatorX64$".toRegex(), "X64"))
                }
            )
        }

        factories.add(defaultFactory<LinuxTarget, _, _>(project, container, "linux", inheritedParents, inheritedNames))
        listOf(
            "linuxX64",
            "linuxArm64",
        ).forEach {
            listOf("clang", "zig").forEach { variant ->
                factories.add(
                    defaultFactory<LinuxTarget, _, _>(
                        project,
                        container,
                        "$it.$variant",
                        inheritedParents,
                        inheritedNames
                    )
                )
            }
            factories.add(
                defaultFactory<LinuxTarget, _, _>(
                    project,
                    container,
                    "$it.konan",
                    inheritedParents,
                    inheritedNames,
                    "linux"
                ) {
                    useKonanAndLld(it)
                }
            )
        }

        factories.add(defaultFactory<MinGWTarget, _, _>(project, container, "mingw", inheritedParents, inheritedNames))
        listOf(
            "mingwX64",
        ).forEach {
            listOf("clang", "zig").forEach { variant ->
                factories.add(
                    defaultFactory<MinGWTarget, _, _>(
                        project,
                        container,
                        "$it.$variant",
                        inheritedParents,
                        inheritedNames
                    )
                )
            }
            factories.add(
                defaultFactory<MinGWTarget, _, _>(
                    project,
                    container,
                    "$it.konan",
                    inheritedParents,
                    inheritedNames,
                    "mingw"
                ) {
                    useKonanAndLld(it)
                }
            )
        }

        factories.add(defaultFactory<DarwinTarget, _, _>(project, container, "macos", inheritedParents, inheritedNames))
        listOf(
            "macosX64",
            "macosArm64",
        ).forEach {
            listOf("xcode", "clang", "zig").forEach { variant ->
                factories.add(
                    defaultFactory<DarwinTarget, _, _>(
                        project,
                        container,
                        "$it.$variant",
                        inheritedParents,
                        inheritedNames
                    )
                )
            }
            factories.add(
                defaultFactory<DarwinTarget, _, _>(
                    project,
                    container,
                    "$it.konan",
                    inheritedParents,
                    inheritedNames,
                    "macos"
                ) {
                    useKonan(it)
                }
            )
        }
    }
}

interface KrossCompileTargetContainerWithPresetFunctions : KrossCompileTargetContainerWithFactories {
    val host
        get() = default<KCHostTarget>("host")

    val android
        get() = default<KCAndroidTarget>("android")
    val androidNativeX64
        get() = ndkWithKonanWithClangWithZig<KCAndroidTarget>("androidX64", "androidNativeX64")
    val androidNativeX86
        get() = ndkWithKonanWithClangWithZig<KCAndroidTarget>("androidX86", "androidNativeX86")
    val androidNativeArm32
        get() = ndkWithKonanWithClangWithZig<KCAndroidTarget>("androidArm32", "androidNativeArm32")
    val androidNativeArm64
        get() = ndkWithKonanWithClangWithZig<KCAndroidTarget>("androidArm64", "androidNativeArm64")

    val ios
        get() = default<KCIOSTarget>("ios")
    val iosArm32
        get() = xcodeWithKonanWithClangWithZig<KCIOSTarget>("iosArm32")
    val iosArm64
        get() = xcodeWithKonanWithClangWithZig<KCIOSTarget>("iosArm64")
    val iosSimulator
        get() = default<KCIOSSimulatorTarget>("iosSimulator")
    val iosSimulatorArm64
        get() = xcodeWithKonanWithClangWithZig<KCIOSSimulatorTarget>("iosSimulatorArm64")
    val iosX64
        get() = xcodeWithKonanWithClangWithZig<KCIOSSimulatorTarget>("iosSimulatorX64", "iosX64")

    val watchos
        get() = default<KCWatchOSTarget>("watchos")
    val watchosArm32
        get() = xcodeWithKonanWithClangWithZig<KCWatchOSTarget>("watchosArm32")
    val watchosArm64
        get() = xcodeWithKonanWithClangWithZig<KCWatchOSTarget>("watchosArm64")
    val watchosSimulator
        get() = default<KCWatchOSSimulatorTarget>("watchosSimulator")
    val watchosSimulatorArm64
        get() = xcodeWithKonanWithClangWithZig<KCWatchOSSimulatorTarget>("watchosSimulatorArm64")
    val watchosX64
        get() = xcodeWithKonanWithClangWithZig<KCWatchOSSimulatorTarget>("watchosSimulatorX64", "watchosX64")

    val tvos
        get() = default<KCTvOSTarget>("tvos")
    val tvosArm64
        get() = xcodeWithKonanWithClangWithZig<KCTvOSTarget>("tvosArm64")
    val tvosSimulator
        get() = default<KCTvOSSimulatorTarget>("tvosSimulator")
    val tvosSimulatorArm64
        get() = xcodeWithKonanWithClangWithZig<KCTvOSSimulatorTarget>("tvosSimulatorArm64")
    val tvosX64
        get() = xcodeWithKonanWithClangWithZig<KCTvOSSimulatorTarget>("tvosSimulatorX64", "tvosX64")

    val linux
        get() = default<KCLinuxTarget>("linux")
    val linuxX64
        get() = konanWithClangWithZig<KCLinuxTarget>("linuxX64")
    val linuxArm64
        get() = konanWithClangWithZig<KCLinuxTarget>("linuxArm64")
    val linuxArm32Hfp
        get() = konanWithClangWithZig<KCLinuxTarget>("linuxArm32Hfp")
    val linuxMips32
        get() = konanWithClangWithZig<KCLinuxTarget>("linuxMips32")
    val linuxMipsel32
        get() = konanWithClangWithZig<KCLinuxTarget>("linuxMipsel32")

    val mingw
        get() = default<KCMinGWTarget>("mingw")
    val mingwX86
        get() = konanWithClangWithZig<KCMinGWTarget>("mingwX86")
    val mingwX64
        get() = konanWithClangWithZig<KCMinGWTarget>("mingwX64")
    val mingwArm64
        get() = konanWithClangWithZig<KCMinGWTarget>("mingwArm64")

    val macos
        get() = default<KCDarwinTarget>("macos")
    val macosX64
        get() = xcodeWithKonanWithClangWithZig<KCDarwinTarget>("macosX64")
    val macosArm64
        get() = xcodeWithKonanWithClangWithZig<KCDarwinTarget>("macosArm64")
}


interface HasBaseFactoryName {
    val baseFactoryName: String
}

interface HasDefaultTargetName {
    val defaultTargetName: String
}

interface HasInlinedHelperFunctions<T : KrossCompileTarget<*>> {
    fun inlinedConfigureOrCreate(
        targetName: String,
        factoryName: String,
        configure: T.() -> Unit,
    ): T

    fun inlinedConfigureOrCreateGeneric(
        targetName: String,
        factoryName: String,
        configure: KCGenericTarget.() -> Unit,
    ): KCGenericTarget
}

interface FunctionsBaseSlim : HasBaseFactoryName, HasDefaultTargetName

interface FunctionsBase<T : KrossCompileTarget<*>> : FunctionsBaseSlim, HasInlinedHelperFunctions<T>

interface DefaultFunctions<T : KrossCompileTarget<*>> : FunctionsBase<T> {
    operator fun invoke(
        name: String = defaultTargetName,
        configure: T.() -> Unit = { }
    ): T =
        inlinedConfigureOrCreate(
            name,
            baseFactoryName,
            configure
        )

    operator fun invoke() = invoke(defaultTargetName) { }
    operator fun invoke(name: String) = invoke(name) { }
    operator fun invoke(name: String, configure: Action<T>) = invoke(name) { configure.execute(this) }
    operator fun invoke(configure: Action<T>) = invoke(defaultTargetName) { configure.execute(this) }
}

interface KonanFunctions<T : KrossCompileTarget<*>> : FunctionsBase<T> {
    fun konan(
        name: String = defaultTargetName,
        configure: T.() -> Unit = { }
    ): T =
        inlinedConfigureOrCreate(
            name,
            "$baseFactoryName.konan",
            configure
        )

    fun konan() = konan(defaultTargetName) { }
    fun konan(name: String) = konan(name) { }
    fun konan(name: String, configure: Action<T>) = konan(name) { configure.execute(this) }
    fun konan(configure: Action<T>) = konan(defaultTargetName) { configure.execute(this) }
}

interface NdkFunctions<T : KrossCompileTarget<*>> : FunctionsBase<T> {
    fun ndk(
        name: String = defaultTargetName,
        configure: T.() -> Unit = { }
    ): T =
        inlinedConfigureOrCreate(
            name,
            "$baseFactoryName.ndk",
            configure
        )

    fun ndk() = ndk(defaultTargetName) { }
    fun ndk(name: String) = ndk(name) { }
    fun ndk(name: String, configure: Action<T>) = ndk(name) { configure.execute(this) }
    fun ndk(configure: Action<T>) = ndk(defaultTargetName) { configure.execute(this) }
}

interface XCodeFunctions<T : KrossCompileTarget<*>> : FunctionsBase<T> {
    fun xcode(
        name: String = defaultTargetName,
        configure: T.() -> Unit = { }
    ): T =
        inlinedConfigureOrCreate(
            name,
            "$baseFactoryName.xcode",
            configure
        )

    fun xcode() = xcode(defaultTargetName) { }
    fun xcode(name: String) = xcode(name) { }
    fun xcode(name: String, configure: Action<T>) = xcode(name) { configure.execute(this) }
    fun xcode(configure: Action<T>) = xcode(defaultTargetName) { configure.execute(this) }
}

interface ClangFunctions<T : KrossCompileTarget<*>> : FunctionsBase<T> {
    fun clang(
        name: String = defaultTargetName,
        configure: T.() -> Unit = { }
    ): T =
        inlinedConfigureOrCreate(
            name,
            "$baseFactoryName.clang",
            configure
        )

    fun clang() = clang(defaultTargetName) { }
    fun clang(name: String) = clang(name) { }
    fun clang(name: String, configure: Action<T>) = clang(name) { configure.execute(this) }
    fun clang(configure: Action<T>) = clang(defaultTargetName) { configure.execute(this) }
}

interface ZigFunctions<T : KrossCompileTarget<*>> : FunctionsBase<T> {
    fun zig(
        name: String = defaultTargetName,
        configure: T.() -> Unit = { }
    ): T =
        inlinedConfigureOrCreate(
            name,
            "$baseFactoryName.zig",
            configure
        )

    fun zig() = zig(defaultTargetName) { }
    fun zig(name: String) = zig(name) { }
    fun zig(name: String, configure: Action<T>) = zig(name) { configure.execute(this) }
    fun zig(configure: Action<T>) = zig(defaultTargetName) { configure.execute(this) }
}

interface KonanWithClangWithZig<T : KrossCompileTarget<*>> : KonanFunctions<T>, ClangFunctions<T>, ZigFunctions<T>
interface XCodeWithKonanWithClangWithZig<T : KrossCompileTarget<*>> : XCodeFunctions<T>, KonanWithClangWithZig<T>
interface NdkWithKonanWithClangWithZig<T : KrossCompileTarget<*>> : NdkFunctions<T>, KonanWithClangWithZig<T>

private inline fun <reified T : KrossCompileTarget<*>> KrossCompileTargetContainerWithFactories.default(
    baseFactoryName: String,
    defaultTargetName: String = baseFactoryName
) =
    object :
        DefaultFunctions<T> {
        override val baseFactoryName: String = baseFactoryName
        override val defaultTargetName: String = defaultTargetName

        @Suppress("UNCHECKED_CAST")
        override fun inlinedConfigureOrCreate(
            targetName: String,
            factoryName: String,
            configure: T.() -> Unit
        ): T = configureOrCreate(
            targetName,
            factories.getByName(factoryName) as KrossCompileTargetFactory<T>,
            configure
        )

        @Suppress("UNCHECKED_CAST")
        override fun inlinedConfigureOrCreateGeneric(
            targetName: String,
            factoryName: String,
            configure: KCGenericTarget.() -> Unit
        ): KCGenericTarget = configureOrCreate(
            targetName,
            factories.getByName(factoryName) as KrossCompileTargetFactory<KCGenericTarget>,
            configure
        )
    }

private inline fun <reified T : KrossCompileTarget<*>> KrossCompileTargetContainerWithFactories.ndkWithKonanWithClangWithZig(
    baseFactoryName: String,
    defaultTargetName: String = baseFactoryName
) =
    object :
        NdkWithKonanWithClangWithZig<T> {
        override val baseFactoryName: String = baseFactoryName
        override val defaultTargetName: String = defaultTargetName

        @Suppress("UNCHECKED_CAST")
        override fun inlinedConfigureOrCreate(
            targetName: String,
            factoryName: String,
            configure: T.() -> Unit
        ): T = configureOrCreate(
            targetName,
            factories.getByName(factoryName) as KrossCompileTargetFactory<T>,
            configure
        )

        @Suppress("UNCHECKED_CAST")
        override fun inlinedConfigureOrCreateGeneric(
            targetName: String,
            factoryName: String,
            configure: KCGenericTarget.() -> Unit
        ): KCGenericTarget = configureOrCreate(
            targetName,
            factories.getByName(factoryName) as KrossCompileTargetFactory<KCGenericTarget>,
            configure
        )
    }

private inline fun <reified T : KrossCompileTarget<*>> KrossCompileTargetContainerWithFactories.xcodeWithKonanWithClangWithZig(
    baseFactoryName: String,
    defaultTargetName: String = baseFactoryName
) =
    object :
        XCodeWithKonanWithClangWithZig<T> {
        override val baseFactoryName: String = baseFactoryName
        override val defaultTargetName: String = defaultTargetName

        @Suppress("UNCHECKED_CAST")
        override fun inlinedConfigureOrCreate(
            targetName: String,
            factoryName: String,
            configure: T.() -> Unit
        ): T = configureOrCreate(
            targetName,
            factories.getByName(factoryName) as KrossCompileTargetFactory<T>,
            configure
        )

        @Suppress("UNCHECKED_CAST")
        override fun inlinedConfigureOrCreateGeneric(
            targetName: String,
            factoryName: String,
            configure: KCGenericTarget.() -> Unit
        ): KCGenericTarget = configureOrCreate(
            targetName,
            factories.getByName(factoryName) as KrossCompileTargetFactory<KCGenericTarget>,
            configure
        )
    }

private inline fun <reified T : KrossCompileTarget<*>> KrossCompileTargetContainerWithFactories.konanWithClangWithZig(
    baseFactoryName: String,
    defaultTargetName: String = baseFactoryName
) =
    object :
        KonanWithClangWithZig<T> {
        override val baseFactoryName: String = baseFactoryName
        override val defaultTargetName: String = defaultTargetName

        @Suppress("UNCHECKED_CAST")
        override fun inlinedConfigureOrCreate(
            targetName: String,
            factoryName: String,
            configure: T.() -> Unit
        ): T = configureOrCreate(
            targetName,
            factories.getByName(factoryName) as KrossCompileTargetFactory<T>,
            configure
        )

        @Suppress("UNCHECKED_CAST")
        override fun inlinedConfigureOrCreateGeneric(
            targetName: String,
            factoryName: String,
            configure: KCGenericTarget.() -> Unit
        ): KCGenericTarget = configureOrCreate(
            targetName,
            factories.getByName(factoryName) as KrossCompileTargetFactory<KCGenericTarget>,
            configure
        )
    }