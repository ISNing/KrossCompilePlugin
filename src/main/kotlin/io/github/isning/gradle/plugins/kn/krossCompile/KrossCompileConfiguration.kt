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