package io.github.isning.gradle.plugins.kn.krossCompile

import io.github.isning.gradle.plugins.cmake.*
import io.github.isning.gradle.plugins.cmake.params.*
import io.github.isning.gradle.plugins.cmake.params.platform.ModifiableHostParamsImpl
import io.github.isning.gradle.plugins.kn.krossCompile.utils.delegateItemsTo
import io.github.isning.gradle.plugins.kn.krossCompile.utils.delegateItemsTransformedTo
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

interface KrossCompileLibrary<T : CMakeProject> : Named, KrossCompileConfiguration<T>, KrossCompileTargetContainer {
    override val cinterop: CInteropSettings
    override val cmake: CMakeLibrarySettings<T>
}

abstract class AbstractKrossCompileLibrary<T : CMakeProject>(
    project: Project,
    private val inheritedParents: List<KrossCompileConfiguration<*>>,
    private val inheritedNames: List<String>,
) :
    AbstractKrossCompileConfiguration<T>(project), KrossCompileLibrary<T> {
    abstract val libraryName: String
    override val rawTargets: NamedDomainObjectContainer<KrossCompileTarget<*>> =
        project.container(KrossCompileTarget::class.java)

    @Suppress("UNCHECKED_CAST")
    val targets: NamedDomainObjectContainer<KrossCompileTargetImpl<ModifiableHostParamsImpl, ModifiableCMakeBuildParamsImpl>> =
        project.container(KrossCompileTargetImpl::class.java) { name: String ->
            KrossCompileTargetImpl(
                project,
                name,
                CMakeTargetImpl(project, name, (inheritedParents + this).map { cmake }, inheritedNames + libraryName, {
                    ModifiableHostParamsImpl()
                }, {
                    ModifiableCMakeBuildParamsImpl()
                }),
                inheritedParents + this,
                inheritedNames + libraryName
            )
        }.apply {
            delegateItemsTo(rawTargets)
        } as NamedDomainObjectContainer<KrossCompileTargetImpl<ModifiableHostParamsImpl, ModifiableCMakeBuildParamsImpl>>

    final override fun getName(): String = libraryName
}

class KrossCompileLibraryImpl(
    project: Project, override val libraryName: String,
    private val inheritedParents: List<KrossCompileConfiguration<*>>,
    private val inheritedNames: List<String>,
    buildParamsInitialOverlay: CMakeParams? = null,
    configParamsInitialOverlay: CMakeParams? = null
) : AbstractKrossCompileLibrary<CMakeProjectImpl>(project, inheritedParents, inheritedNames),
    KrossCompileTargetContainerWithPresetFunctions,
    KrossCompileTargetContainerWithFactoriesRegisterer {

    override val cmake: CMakeLibrarySettingsImpl<CMakeProjectImpl> =
        CMakeLibrarySettingsImpl(
            project,
            CMakeProjectImpl(
                project,
                libraryName,
                inheritedParents.map { it.cmake },
                inheritedNames,
                { buildParamsInitialOverlay },
                {
                    sourceDir?.let {
                        ModifiableCMakeGeneralParamsImpl().apply {
                            sourceDir = this@KrossCompileLibraryImpl.sourceDir
                        } + configParamsInitialOverlay.orEmpty
                    } ?: configParamsInitialOverlay
                },
            )
        )
    override val factories: NamedDomainObjectCollection<KrossCompileTargetFactory<*>> =
        project.container(KrossCompileTargetFactory::class.java)

    init {
        registerFactories(project, cmake.delegate, inheritedParents + this, inheritedNames + libraryName)
    }

    init {
        @OptIn(ManagedByKrossCompile::class)
        rawTargets.delegateItemsTransformedTo(cmake.rawTargets) { it.cmake }
    }
}
