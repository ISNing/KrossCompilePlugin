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
    AbstractKrossCompileConfiguration<ModifiableCMakeConfiguration<ModifiableCMakeGeneralParams, ModifiableCMakeBuildParams>>(
        project
    ) {
    override val cmake: CMakeSettings<ModifiableCMakeConfiguration<ModifiableCMakeGeneralParams, ModifiableCMakeBuildParams>> =
        CMakeSettingsImpl(project, project.cmakeExtension.delegateWith({ null }, {
            sourceDir?.let {
                ModifiableCMakeGeneralParamsImpl().apply {
                    sourceDir = this@KrossCompileExtension.sourceDir
                }
            } }))// TODO: cmakeExtension must be created before this extension

    val rawLibraries: NamedDomainObjectContainer<KrossCompileLibrary<*>> =
        project.container(KrossCompileLibrary::class.java).apply {
            delegateItemsTransformedTo(project.cmakeExtension.rawProjects) { it.cmake }
        }

    val libraries: NamedDomainObjectContainer<KrossCompileLibraryImpl> =
        project.container(KrossCompileLibraryImpl::class.java) {
            KrossCompileLibraryImpl(project, it, listOf(this), emptyList())
        }
}
