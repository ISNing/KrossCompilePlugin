plugins {
    kotlin("jvm") version "1.9.21"
    id("com.gradle.plugin-publish") version "1.0.0"
    `kotlin-dsl`
    id("idea")
}

group = "io.github.isning.gradle.plugins.kn"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("io.github.isning.gradle.plugins:CMakePlugin:0.1.1")
    implementation("org.jetbrains.kotlin.multiplatform:org.jetbrains.kotlin.multiplatform.gradle.plugin:1.9.21")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

gradlePlugin {
    website.set("https://github.com/ISNing/KrossCompilePlugin")
    vcsUrl.set("https://github.com/ISNing/KrossCompilePlugin")
    plugins {
        create("krossCompile") {
            id = "io.github.isning.gradle.plugins.kn.krossCompile"
            implementationClass = "io.github.isning.gradle.plugins.kn.krossCompile.KrossCompilePlugin"
            displayName = "Kotlin/Native CInterop Crosscompile Plugin"
            description = "Automatically crosscompile Kotlin/Native CInterop libraries for all supported targets"
            tags.set(listOf("kotlin", "native", "cinterop", "crosscompile"))
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}