# KrossCompilePlugin

## Overview

The KrossCompilePlugin is a Gradle plugin designed to simplify the integration of CMake projects into Kotlin/Native
projects. This is achieved by cross-compiling the CMake project for various targets and setting the Kotlin/Native's
CInterop tool to link it as a static binary into a klib file.

## Key Features

1. **Cross-Compilation**: The plugin supports cross-compiling CMake projects for multiple platforms including Android,
Linux, Windows, and various Apple operating systems.
2. **CInterop Integration**: The plugin integrates with Kotlin/Native's CInterop tool to link the compiled CMake project
as a static binary into a klib file.
3. **Customizable Build Configurations**: The plugin allows you to define different building configurations/flags for
each target in the gradle script.

## Prerequisites

* `CMake` installed on the system. Available [here](https://www.cmake.org "CMake Homepage").
* `Android NDK`, `XCode`, `Clang`, `Zig` or any toolchain thing you'll need during the compilation progress.

## Installation

Apply the plugin to your project:

```kotlin
plugins {
    id("io.github.isning.gradle.plugins.kn.krossCompile") version "1.0.0"
}
```

## Usage

Here is an example of how to use the KrossCompilePlugin:

```kotlin
krossCompile {
    libraries {
        val zxingCpp by creating {
            // Define the source directory of the CMake project
            sourceDir = file("../zxing-cpp").absolutePath
            outputPath = ""

            // Setup CInterop
            cinterop {
                packageName = "zxing.cinterop"
                headers = listOf("zxing-c.h")
                includeDirs.from("$sourceDir/core/src")
            }

            // Define CMake configurations
            cmake.apply {
                val buildPath = project.layout.buildDirectory.dir("cmake").get().asFile.absolutePath +
                        "/{projectName}/{targetName}"
                outputPath = "core"
                libraryArtifactNames = listOf("libZXing.a")
                configParams {
                    buildDir = buildPath
                }
                configParams += ModifiablePlatformEntriesImpl().apply {
                    buildType = "Release"
                    buildSharedLibs = false
                }.asCMakeParams
                buildParams {
                    buildDir = buildPath
                    config = "Release"
                }
                // Multithreaded compilation
                buildParams += CustomCMakeParams(listOf("-j16"))
            }

            // Define target platforms
            androidNativeX64.ndk()
            androidNativeX86.ndk()
            androidNativeArm32.ndk()
            androidNativeArm64.ndk()

            linuxX64.clang()
            linuxArm64.clang()
            mingwX64.clang()

            if (hostOs == "Mac OS X") {
                iosX64.xcode()
                iosArm64.xcode()
                iosArm64.xcode("iosSimulatorArm64")
                macosX64.xcode()
                macosArm64.xcode()
                watchosX64.xcode()
                watchosArm32.xcode()
                watchosArm64.xcode()
                watchosArm64.xcode("watchosSimulatorArm64")
                tvosX64.xcode()
                tvosArm64.xcode()
                tvosArm64.xcode("tvosSimulatorArm64")
            }
        }
    }
}
```

## Properties

- `sourceDir`: Defines the source directory of the CMake project.
- `outputPath`: Defines the output path of the compiled library.
- `cinterop`: Configures the CInterop tool (properties definition are aligned to K/N's cinterop configuration).
    - `packageName`: Specifies the package name for the generated bindings.
    - `headers`: Lists the header files to generate bindings for.
    - `includeDirs`: Specifies the directories to search for header files.
    - ... For more properties, you can read c interoperation part of K/N official document.
- `cmake`: Configures the CMake build.
    - `outputPath`: Specifies the output binary's relative path of from the build directory.
    - `libraryArtifactNames`: Lists the names of the library artifacts to be produced by the CMake build.
As well as static library's names.
    - `configParams`: Defines the configuration parameters for the CMake build.
    - `buildParams`: Defines the build parameters for the CMake build.
- `androidNative[arch]/{ndk,clang,zig}()`: Configures the Android targets. Replace `[arch]` with the architecture
(e.g., `X64`, `X86`, `Arm32`, `Arm64`) and choose the compiler (`ndk`, `clang`, `zig`).
- `linux[arch].{clang, zig}()`: Configures the Linux targets. Replace `[arch]` with the architecture
(e.g., `X64`, `Arm64`) and choose the compiler (`clang`, `zig`).
- `[apple target][arch].{xcode, clang, zig}()`: Configures the Apple targets. Replace `[apple target]` with the target
platform (e.g., `ios`, `macos`, `watchos`, `tvos`), `[arch]` with the architecture (e.g., `X64`, `Arm64`) and choose the
compiler (`xcode`, `clang`, `zig`).

*Note*: To know more about configuring `cmake` block, see [CMakePlugin](https://github.com/ISNing/CMakePlugin)

## Support

For any issues or feature requests, please create an issue.

## License

The KrossCompilePlugin is licensed under the Apache License, Version 2.0 with no warranty (expressed or implied) for any
purpose.