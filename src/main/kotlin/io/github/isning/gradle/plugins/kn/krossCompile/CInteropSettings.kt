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

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File

interface CInteropSettings : CInteropDefFileConfiguration {
    val defFileOutput: File
    val includeDirs: ConfigurableFileCollection
}

interface ModifiableCInteropSettings : CInteropSettings, ModifiableCInteropDefFileConfiguration {
    override var includeDirs: ConfigurableFileCollection
    override var defFileOutput: File
}

operator fun <T : CInteropSettings> T.invoke(configure: T.() -> Unit) = apply(configure)

open class CInteropSettingsImpl(val project: Project) : ModifiableCInteropSettings {
    val packageNameProp: Property<String> = project.objects.property(String::class.java)
    val headersProp: ListProperty<String> = project.objects.listProperty(String::class.java)
    val libraryPathsProp: ListProperty<String> = project.objects.listProperty(String::class.java)
    val staticLibrariesProp: ListProperty<String> = project.objects.listProperty(String::class.java)
    val compilerOptsProp: ListProperty<String> = project.objects.listProperty(String::class.java)
    val linkerOptsProp: ListProperty<String> = project.objects.listProperty(String::class.java)
    val headerFilterProp: ListProperty<String> = project.objects.listProperty(String::class.java)
    val excludeFilterProp: ListProperty<String> = project.objects.listProperty(String::class.java)
    val userSetupHintProp: Property<String?> = project.objects.property(String::class.java)
    val excludeDependentModulesProp: Property<Boolean> = project.objects.property(Boolean::class.java)
    val excludedFunctionsProp: ListProperty<String> = project.objects.listProperty(String::class.java)
    val strictEnumsProp: ListProperty<String> = project.objects.listProperty(String::class.java)
    val nonStrictEnumsProp: ListProperty<String> = project.objects.listProperty(String::class.java)
    val noStringConversionProp: ListProperty<String> = project.objects.listProperty(String::class.java)

    val defFileOutputProp: RegularFileProperty = project.objects.fileProperty()

    override var includeDirs: ConfigurableFileCollection = files()

    constructor(project: Project, origin: CInteropSettings) : this(project) {
        packageName = origin.packageName
        headers = origin.headers
        libraryPaths = origin.libraryPaths
        staticLibraries = origin.staticLibraries
        compilerOpts = origin.compilerOpts
        linkerOpts = origin.linkerOpts
        headerFilter = origin.headerFilter
        excludeFilter = origin.excludeFilter
        userSetupHint = origin.userSetupHint
        excludeDependentModules = origin.excludeDependentModules
        excludedFunctions = origin.excludedFunctions
        strictEnums = origin.strictEnums
        nonStrictEnums = origin.nonStrictEnums
        noStringConversion = origin.noStringConversion
        defFileOutput = origin.defFileOutput
    }

    override var packageName: String?
        get() = packageNameProp.orNull
        set(value) = packageNameProp.set(value)

    override var headers: List<String>
        get() = headersProp.get()
        set(value) = headersProp.set(value)
    override var libraryPaths: List<String>
        get() = libraryPathsProp.get()
        set(value) = libraryPathsProp.set(value)
    override var staticLibraries: List<String>
        get() = staticLibrariesProp.get()
        set(value) = staticLibrariesProp.set(value)
    override var compilerOpts: List<String>
        get() = includeDirs.files.map { "-I${it.absolutePath}" } + compilerOptsProp.get()
        set(value) = compilerOptsProp.set(value)
    override var linkerOpts: List<String>
        get() = linkerOptsProp.get()
        set(value) = linkerOptsProp.set(value)
    override var headerFilter: List<String>
        get() = headerFilterProp.get()
        set(value) = headerFilterProp.set(value)
    override var excludeFilter: List<String>
        get() = excludeFilterProp.get()
        set(value) = excludeFilterProp.set(value)
    override var userSetupHint: String?
        get() = userSetupHintProp.orNull
        set(value) = userSetupHintProp.set(value)
    override var excludeDependentModules: Boolean?
        get() = excludeDependentModulesProp.orNull
        set(value) = excludeDependentModulesProp.set(value)

    override var excludedFunctions: List<String>
        get() = excludedFunctionsProp.get()
        set(value) = excludedFunctionsProp.set(value)

    override var strictEnums: List<String>
        get() = strictEnumsProp.get()
        set(value) = strictEnumsProp.set(value)

    override var nonStrictEnums: List<String>
        get() = nonStrictEnumsProp.get()
        set(value) = nonStrictEnumsProp.set(value)

    override var noStringConversion: List<String>
        get() = noStringConversionProp.get()
        set(value) = noStringConversionProp.set(value)

    override var defFileOutput: File
        get() = defFileOutputProp.orNull?.asFile ?: error("defFileOutput is not set")
        set(value) = defFileOutputProp.set(value)

    fun packageName(value: String) {
        packageName = value
    }

    private fun files() = project.objects.fileCollection()
    private fun files(vararg paths: Any) = project.objects.fileCollection().from(*paths)

    fun header(file: Any) = headers(file)
    fun headers(vararg files: Any) = headers(files(files))
    fun headers(files: FileCollection) {
        headers += files.map { it.absolutePath }
    }

    fun includeDirs(vararg values: Any) = includeDirs.from(values.toList())

    fun compilerOpts(vararg values: String) = compilerOpts(values.toList())
    fun compilerOpts(values: List<String>) {
        compilerOpts += values
    }

    fun linkerOpts(vararg values: String) = linkerOpts(values.toList())
    fun linkerOpts(values: List<String>) {
        linkerOpts += values
    }
}

fun CInteropSettings.toModifiable(project: Project, configure: CInteropSettingsImpl.() -> Unit = {}) = CInteropSettingsImpl(project, this).apply(configure)