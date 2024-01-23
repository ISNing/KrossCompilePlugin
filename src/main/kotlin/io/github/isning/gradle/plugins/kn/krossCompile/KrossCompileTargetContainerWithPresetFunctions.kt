package io.github.isning.gradle.plugins.kn.krossCompile

import io.github.isning.gradle.plugins.cmake.*
import io.github.isning.gradle.plugins.cmake.params.ModifiableCMakeBuildParams
import io.github.isning.gradle.plugins.cmake.params.ModifiableCMakeGeneralParams
import io.github.isning.gradle.plugins.cmake.params.platform.*
import io.github.isning.gradle.plugins.cmake.targets.*
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
        factories.add(defaultFactory<HostTarget, _, _>(project, container, "host", inheritedParents, inheritedNames))
        listOf(
            "androidNativeX64" to "androidX64",
            "androidNativeX86" to "androidX86",
            "androidNativeArm32" to "androidArm32",
            "androidNativeArm64" to "androidArm64",
        ).forEach {
            factories.add(
                defaultFactory<AndroidTarget, _, _>(
                    project, container, it.first, inheritedParents, inheritedNames, it.second
                )
            )
        }
        listOf(
            "iosArm32",
            "iosArm64",
            "iosX64",
        ).forEach {
            factories.add(defaultFactory<IOSTarget, _, _>(project, container, it, inheritedParents, inheritedNames))
        }
        listOf(
            "watchosArm32",
            "watchosArm64",
            "watchosX86",
            "watchosX64",
        ).forEach {
            factories.add(defaultFactory<WatchOSTarget, _, _>(project, container, it, inheritedParents, inheritedNames))
        }
        listOf(
            "tvosArm64",
            "tvosX64",
        ).forEach {
            factories.add(defaultFactory<TvOSTarget, _, _>(project, container, it, inheritedParents, inheritedNames))
        }
        listOf(
            "linuxX64",
            "linuxArm64",
            "linuxArm32Hfp",
            "linuxMips32",
            "linuxMipsel32",
        ).forEach {
            factories.add(defaultFactory<LinuxTarget, _, _>(project, container, it, inheritedParents, inheritedNames))
        }
        listOf(
            "msvcX86",
            "msvcX64",
        ).forEach {
            factories.add(defaultFactory<MSVCTarget, _, _>(project, container, it, inheritedParents, inheritedNames))
        }
        listOf(
            "mingwX86",
            "mingwX64",
        ).forEach {
            factories.add(defaultFactory<MinGWTarget, _, _>(project, container, it, inheritedParents, inheritedNames))
        }
        listOf(
            "macosX64",
            "macosArm64",
        ).forEach {
            factories.add(defaultFactory<DarwinTarget, _, _>(project, container, it, inheritedParents, inheritedNames))
        }
    }
}

interface KrossCompileTargetContainerWithPresetFunctions : KrossCompileTargetContainerWithFactories {
    @Suppress("UNCHECKED_CAST")
    fun host(
        name: String = "host", configure: KCHostTarget.() -> Unit = { }
    ): KCHostTarget = configureOrCreate(
        name, factories.getByName("host") as KrossCompileTargetFactory<KCHostTarget>, configure
    )

    fun host() = host("host") { }
    fun host(name: String) = host(name) { }
    fun host(name: String, configure: Action<KCHostTarget>) = host(name) { configure.execute(this) }
    fun host(configure: Action<KCHostTarget>) = host("host") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun androidNativeX64(
        name: String = "androidNativeX64", configure: KCAndroidTarget.() -> Unit = { }
    ): KCAndroidTarget = configureOrCreate(
        name, factories.getByName("androidNativeX64") as KrossCompileTargetFactory<KCAndroidTarget>, configure
    )

    fun androidNativeX64() = androidNativeX64("androidNativeX64") { }
    fun androidNativeX64(name: String) = androidNativeX64(name) { }
    fun androidNativeX64(name: String, configure: Action<KCAndroidTarget>) =
        androidNativeX64(name) { configure.execute(this) }

    fun androidNativeX64(configure: Action<KCAndroidTarget>) =
        androidNativeX64("androidNativeX64") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun androidNativeX86(
        name: String = "androidNativeX86", configure: KCAndroidTarget.() -> Unit = { }
    ): KCAndroidTarget = configureOrCreate(
        name, factories.getByName("androidNativeX86") as KrossCompileTargetFactory<KCAndroidTarget>, configure
    )

    fun androidNativeX86() = androidNativeX86("androidNativeX86") { }
    fun androidNativeX86(name: String) = androidNativeX86(name) { }
    fun androidNativeX86(name: String, configure: Action<KCAndroidTarget>) =
        androidNativeX86(name) { configure.execute(this) }

    fun androidNativeX86(configure: Action<KCAndroidTarget>) =
        androidNativeX86("androidNativeX86") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun androidNativeArm32(
        name: String = "androidNativeArm32", configure: KCAndroidTarget.() -> Unit = { }
    ): KCAndroidTarget = configureOrCreate(
        name, factories.getByName("androidNativeArm32") as KrossCompileTargetFactory<KCAndroidTarget>, configure
    )

    fun androidNativeArm32() = androidNativeArm32("androidNativeArm32") { }
    fun androidNativeArm32(name: String) = androidNativeArm32(name) { }
    fun androidNativeArm32(name: String, configure: Action<KCAndroidTarget>) =
        androidNativeArm32(name) { configure.execute(this) }

    fun androidNativeArm32(configure: Action<KCAndroidTarget>) =
        androidNativeArm32("androidNativeArm32") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun androidNativeArm64(
        name: String = "androidNativeArm64", configure: KCAndroidTarget.() -> Unit = { }
    ): KCAndroidTarget = configureOrCreate(
        name, factories.getByName("androidNativeArm64") as KrossCompileTargetFactory<KCAndroidTarget>, configure
    )

    fun androidNativeArm64() = androidNativeArm64("androidNativeArm64") { }
    fun androidNativeArm64(name: String) = androidNativeArm64(name) { }
    fun androidNativeArm64(name: String, configure: Action<KCAndroidTarget>) =
        androidNativeArm64(name) { configure.execute(this) }

    fun androidNativeArm64(configure: Action<KCAndroidTarget>) =
        androidNativeArm64("androidNativeArm64") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun iosArm32(
        name: String = "iosArm32", configure: KCIOSTarget.() -> Unit = { }
    ): KCIOSTarget = configureOrCreate(
        name, factories.getByName("iosArm32") as KrossCompileTargetFactory<KCIOSTarget>, configure
    )

    fun iosArm32() = iosArm32("iosArm32") { }
    fun iosArm32(name: String) = iosArm32(name) { }
    fun iosArm32(name: String, configure: Action<KCIOSTarget>) = iosArm32(name) { configure.execute(this) }
    fun iosArm32(configure: Action<KCIOSTarget>) = iosArm32("iosArm32") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun iosArm64(
        name: String = "iosArm64", configure: KCIOSTarget.() -> Unit = { }
    ): KCIOSTarget = configureOrCreate(
        name, factories.getByName("iosArm64") as KrossCompileTargetFactory<KCIOSTarget>, configure
    )

    fun iosArm64() = iosArm64("iosArm64") { }
    fun iosArm64(name: String) = iosArm64(name) { }
    fun iosArm64(name: String, configure: Action<KCIOSTarget>) = iosArm64(name) { configure.execute(this) }
    fun iosArm64(configure: Action<KCIOSTarget>) = iosArm64("iosArm64") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun iosX64(
        name: String = "iosX64", configure: KCIOSTarget.() -> Unit = { }
    ): KCIOSTarget = configureOrCreate(
        name, factories.getByName("iosX64") as KrossCompileTargetFactory<KCIOSTarget>, configure
    )

    fun iosX64() = iosX64("iosX64") { }
    fun iosX64(name: String) = iosX64(name) { }
    fun iosX64(name: String, configure: Action<KCIOSTarget>) = iosX64(name) { configure.execute(this) }
    fun iosX64(configure: Action<KCIOSTarget>) = iosX64("iosX64") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun watchosArm32(
        name: String = "watchosArm32", configure: KCWatchOSTarget.() -> Unit = { }
    ): KCWatchOSTarget = configureOrCreate(
        name, factories.getByName("watchosArm32") as KrossCompileTargetFactory<KCWatchOSTarget>, configure
    )

    fun watchosArm32() = watchosArm32("watchosArm32") { }
    fun watchosArm32(name: String) = watchosArm32(name) { }
    fun watchosArm32(name: String, configure: Action<KCWatchOSTarget>) = watchosArm32(name) { configure.execute(this) }
    fun watchosArm32(configure: Action<KCWatchOSTarget>) = watchosArm32("watchosArm32") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun watchosArm64(
        name: String = "watchosArm64", configure: KCWatchOSTarget.() -> Unit = { }
    ): KCWatchOSTarget = configureOrCreate(
        name, factories.getByName("watchosArm64") as KrossCompileTargetFactory<KCWatchOSTarget>, configure
    )

    fun watchosArm64() = watchosArm64("watchosArm64") { }
    fun watchosArm64(name: String) = watchosArm64(name) { }
    fun watchosArm64(name: String, configure: Action<KCWatchOSTarget>) = watchosArm64(name) { configure.execute(this) }
    fun watchosArm64(configure: Action<KCWatchOSTarget>) = watchosArm64("watchosArm64") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun watchosX86(
        name: String = "watchosX86", configure: KCWatchOSTarget.() -> Unit = { }
    ): KCWatchOSTarget = configureOrCreate(
        name, factories.getByName("watchosX86") as KrossCompileTargetFactory<KCWatchOSTarget>, configure
    )

    fun watchosX86() = watchosX86("watchosX86") { }
    fun watchosX86(name: String) = watchosX86(name) { }
    fun watchosX86(name: String, configure: Action<KCWatchOSTarget>) = watchosX86(name) { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun watchosX64(
        name: String = "watchosX64", configure: KCWatchOSTarget.() -> Unit = { }
    ): KCWatchOSTarget = configureOrCreate(
        name, factories.getByName("watchosX64") as KrossCompileTargetFactory<KCWatchOSTarget>, configure
    )

    fun watchosX64() = watchosX64("watchosX64") { }
    fun watchosX64(name: String) = watchosX64(name) { }
    fun watchosX64(name: String, configure: Action<KCWatchOSTarget>) = watchosX64(name) { configure.execute(this) }
    fun watchosX64(configure: Action<KCWatchOSTarget>) = watchosX64("watchosX64") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun tvosArm64(
        name: String = "tvosArm64", configure: KCTvOSTarget.() -> Unit = { }
    ): KCTvOSTarget = configureOrCreate(
        name, factories.getByName("tvosArm64") as KrossCompileTargetFactory<KCTvOSTarget>, configure
    )

    fun tvosArm64() = tvosArm64("tvosArm64") { }
    fun tvosArm64(name: String) = tvosArm64(name) { }
    fun tvosArm64(name: String, configure: Action<KCTvOSTarget>) = tvosArm64(name) { configure.execute(this) }
    fun tvosArm64(configure: Action<KCTvOSTarget>) = tvosArm64("tvosArm64") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun tvosX64(
        name: String = "tvosX64", configure: KCTvOSTarget.() -> Unit = { }
    ): KCTvOSTarget = configureOrCreate(
        name, factories.getByName("tvosX64") as KrossCompileTargetFactory<KCTvOSTarget>, configure
    )

    fun tvosX64() = tvosX64("tvosX64") { }
    fun tvosX64(name: String) = tvosX64(name) { }
    fun tvosX64(name: String, configure: Action<KCTvOSTarget>) = tvosX64(name) { configure.execute(this) }
    fun tvosX64(configure: Action<KCTvOSTarget>) = tvosX64("tvosX64") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun linuxX64(
        name: String = "linuxX64", configure: KCLinuxTarget.() -> Unit = { }
    ): KCLinuxTarget = configureOrCreate(
        name, factories.getByName("linuxX64") as KrossCompileTargetFactory<KCLinuxTarget>, configure
    )

    fun linuxX64() = linuxX64("linuxX64") { }
    fun linuxX64(name: String) = linuxX64(name) { }
    fun linuxX64(name: String, configure: Action<KCLinuxTarget>) = linuxX64(name) { configure.execute(this) }
    fun linuxX64(configure: Action<KCLinuxTarget>) = linuxX64("linuxX64") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun linuxArm64(
        name: String = "linuxArm64", configure: KCLinuxTarget.() -> Unit = { }
    ): KCLinuxTarget = configureOrCreate(
        name, factories.getByName("linuxArm64") as KrossCompileTargetFactory<KCLinuxTarget>, configure
    )

    fun linuxArm64() = linuxArm64("linuxArm64") { }
    fun linuxArm64(name: String) = linuxArm64(name) { }
    fun linuxArm64(name: String, configure: Action<KCLinuxTarget>) = linuxArm64(name) { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun linuxArm32Hfp(
        name: String = "linuxArm32Hfp", configure: KCLinuxTarget.() -> Unit = { }
    ): KCLinuxTarget = configureOrCreate(
        name, factories.getByName("linuxArm32Hfp") as KrossCompileTargetFactory<KCLinuxTarget>, configure
    )

    fun linuxArm32Hfp() = linuxArm32Hfp("linuxArm32Hfp") { }

    fun linuxArm32Hfp(name: String) = linuxArm32Hfp(name) { }

    fun linuxArm32Hfp(name: String, configure: Action<KCLinuxTarget>) = linuxArm32Hfp(name) { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun linuxMips32(
        name: String = "linuxMips32", configure: KCLinuxTarget.() -> Unit = { }
    ): KCLinuxTarget = configureOrCreate(
        name, factories.getByName("linuxMips32") as KrossCompileTargetFactory<KCLinuxTarget>, configure
    )

    fun linuxMips32() = linuxMips32("linuxMips32") { }
    fun linuxMips32(name: String) = linuxMips32(name) { }
    fun linuxMips32(name: String, configure: Action<KCLinuxTarget>) = linuxMips32(name) { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun linuxMipsel32(
        name: String = "linuxMipsel32", configure: KCLinuxTarget.() -> Unit = { }
    ): KCLinuxTarget = configureOrCreate(
        name, factories.getByName("linuxMipsel32") as KrossCompileTargetFactory<KCLinuxTarget>, configure
    )


    fun linuxMipsel32() = linuxMipsel32("linuxMipsel32") { }
    fun linuxMipsel32(name: String) = linuxMipsel32(name) { }
    fun linuxMipsel32(name: String, configure: Action<KCLinuxTarget>) = linuxMipsel32(name) { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun msvcX86(
        name: String = "msvcX86", configure: KCMSVCTarget.() -> Unit = { }
    ): KCMSVCTarget = configureOrCreate(
        name, factories.getByName("msvcX86") as KrossCompileTargetFactory<KCMSVCTarget>, configure
    )

    fun msvcX86() = msvcX86("msvcX86") { }
    fun msvcX86(name: String) = msvcX86(name) { }
    fun msvcX86(name: String, configure: Action<KCMSVCTarget>) = msvcX86(name) { configure.execute(this) }
    fun msvcX86(configure: Action<KCMSVCTarget>) = msvcX86("msvcX86") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun msvcX64(
        name: String = "msvcX64", configure: KCMSVCTarget.() -> Unit = { }
    ): KCMSVCTarget = configureOrCreate(
        name, factories.getByName("msvcX64") as KrossCompileTargetFactory<KCMSVCTarget>, configure
    )

    fun msvcX64() = msvcX64("msvcX64") { }
    fun msvcX64(name: String) = msvcX64(name) { }
    fun msvcX64(name: String, configure: Action<KCMSVCTarget>) = msvcX64(name) { configure.execute(this) }
    fun msvcX64(configure: Action<KCMSVCTarget>) = msvcX64("msvcX64") { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun mingwX86(
        name: String = "mingwX86", configure: KCMinGWTarget.() -> Unit = { }
    ): KCMinGWTarget = configureOrCreate(
        name, factories.getByName("mingwX86") as KrossCompileTargetFactory<KCMinGWTarget>, configure
    )

    fun mingwX86() = mingwX86("mingwX86") { }

    fun mingwX86(name: String) = mingwX86(name) { }

    fun mingwX86(name: String, configure: Action<KCMinGWTarget>) = mingwX86(name) { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun mingwX64(
        name: String = "mingwX64", configure: KCMinGWTarget.() -> Unit = { }
    ): KCMinGWTarget = configureOrCreate(
        name, factories.getByName("mingwX64") as KrossCompileTargetFactory<KCMinGWTarget>, configure
    )

    fun mingwX64() = mingwX64("mingwX64") { }
    fun mingwX64(name: String) = mingwX64(name) { }
    fun mingwX64(name: String, configure: Action<KCMinGWTarget>) = mingwX64(name) { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun macosX64(
        name: String = "macosX64", configure: KCDarwinTarget.() -> Unit = { }
    ): KCDarwinTarget = configureOrCreate(
        name, factories.getByName("macosX64") as KrossCompileTargetFactory<KCDarwinTarget>, configure
    )

    fun macosX64() = macosX64("macosX64") { }
    fun macosX64(name: String) = macosX64(name) { }
    fun macosX64(name: String, configure: Action<KCDarwinTarget>) = macosX64(name) { configure.execute(this) }

    @Suppress("UNCHECKED_CAST")
    fun macosArm64(
        name: String = "macosArm64", configure: KCDarwinTarget.() -> Unit = { }
    ): KCDarwinTarget = configureOrCreate(
        name, factories.getByName("macosArm64") as KrossCompileTargetFactory<KCDarwinTarget>, configure
    )

    fun macosArm64() = macosArm64("macosArm64") { }
    fun macosArm64(name: String) = macosArm64(name) { }
    fun macosArm64(name: String, configure: Action<KCDarwinTarget>) = macosArm64(name) { configure.execute(this) }
}