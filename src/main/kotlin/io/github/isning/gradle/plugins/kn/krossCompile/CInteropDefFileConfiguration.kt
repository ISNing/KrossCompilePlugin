package io.github.isning.gradle.plugins.kn.krossCompile

import io.github.isning.gradle.plugins.kn.krossCompile.utils.warpQuotesJava
import java.io.Serializable

interface CInteropDefFileConfiguration : Serializable {
    val packageName: String?
    val headers: List<String>
    val libraryPaths: List<String>
    val staticLibraries: List<String>
    val compilerOpts: List<String>
    val linkerOpts: List<String>
    val headerFilter: List<String>
    val excludeFilter: List<String>
    val userSetupHint: String?
    val excludeDependentModules: Boolean?
    val excludedFunctions: List<String>
    val strictEnums: List<String>
    val nonStrictEnums: List<String>
    val noStringConversion: List<String>
}

interface ModifiableCInteropDefFileConfiguration : CInteropDefFileConfiguration {
    override var packageName: String?
    override var headers: List<String>
    override var libraryPaths: List<String>
    override var staticLibraries: List<String>
    override var compilerOpts: List<String>
    override var linkerOpts: List<String>
    override var headerFilter: List<String>
    override var excludeFilter: List<String>
    override var userSetupHint: String?
    override var excludeDependentModules: Boolean?
    override var excludedFunctions: List<String>
    override var strictEnums: List<String>
    override var nonStrictEnums: List<String>
    override var noStringConversion: List<String>
}

class CustomCInteropDefFileConfiguration(
    override val packageName: String? = null,
    override val headers: List<String> = emptyList(),
    override val libraryPaths: List<String> = emptyList(),
    override val staticLibraries: List<String> = emptyList(),
    override val compilerOpts: List<String> = emptyList(),
    override val linkerOpts: List<String> = emptyList(),
    override val headerFilter: List<String> = emptyList(),
    override val excludeFilter: List<String> = emptyList(),
    override val userSetupHint: String? = null,
    override val excludeDependentModules: Boolean? = null,
    override val excludedFunctions: List<String> = emptyList(),
    override val strictEnums: List<String> = emptyList(),
    override val nonStrictEnums: List<String> = emptyList(),
    override val noStringConversion: List<String> = emptyList(),
) : CInteropDefFileConfiguration, Serializable

operator fun CInteropDefFileConfiguration.plus(other: CInteropDefFileConfiguration): CustomCInteropDefFileConfiguration =
    CustomCInteropDefFileConfiguration(
        packageName = other.packageName ?: packageName,
        headers = headers + other.headers,
        libraryPaths = libraryPaths + other.libraryPaths,
        staticLibraries = staticLibraries + other.staticLibraries,
        compilerOpts = compilerOpts + other.compilerOpts,
        linkerOpts = linkerOpts + other.linkerOpts,
        headerFilter = headerFilter + other.headerFilter,
        excludeFilter = excludeFilter + other.excludeFilter,
        userSetupHint = other.userSetupHint ?: userSetupHint,
        excludeDependentModules = other.excludeDependentModules ?: excludeDependentModules,
        excludedFunctions = excludedFunctions + other.excludedFunctions,
        strictEnums = strictEnums + other.strictEnums,
        nonStrictEnums = nonStrictEnums + other.nonStrictEnums,
        noStringConversion = noStringConversion + other.noStringConversion
    )

val CInteropDefFileConfiguration.content: String
    get() {
        var content = ""
        packageName?.let { content += "package = $it\n" }
        if (headers.isNotEmpty())
            content += "headers = ${headers.warpQuotesJava().joinToString(" ")}\n"
        if (libraryPaths.isNotEmpty())
            content += "libraryPaths = ${libraryPaths.warpQuotesJava().joinToString(" ")}\n"
        if (staticLibraries.isNotEmpty())
            content += "staticLibraries = ${staticLibraries.warpQuotesJava().joinToString(" ")}\n"
        if (compilerOpts.isNotEmpty())
            content += "compilerOpts = ${compilerOpts.warpQuotesJava().joinToString(" ")}\n"
        if (linkerOpts.isNotEmpty())
            content += "linkerOpts = ${linkerOpts.warpQuotesJava().joinToString(" ")}\n"
        if (headerFilter.isNotEmpty())
            content += "headerFilter = ${headerFilter.warpQuotesJava().joinToString(" ")}\n"
        if (excludeFilter.isNotEmpty())
            content += "excludeFilter = ${excludeFilter.warpQuotesJava().joinToString(" ")}\n"
        userSetupHint?.let { content += "userSetupHint = $it\n" }
        excludeDependentModules?.let { content += "excludeDependentModules = $it\n" }
        if (excludedFunctions.isNotEmpty())
            content += "excludedFunctions = ${excludedFunctions.warpQuotesJava().joinToString(" ")}\n"
        if (strictEnums.isNotEmpty())
            content += "strictEnums = ${strictEnums.warpQuotesJava().joinToString(" ")}\n"
        if (nonStrictEnums.isNotEmpty())
            content += "nonStrictEnums = ${nonStrictEnums.warpQuotesJava().joinToString(" ")}\n"
        if (noStringConversion.isNotEmpty())
            content += "noStringConversion = ${noStringConversion.warpQuotesJava().joinToString(" ")}\n"
        return content
    }
