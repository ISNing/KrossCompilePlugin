import java.util.*

plugins {
    kotlin("jvm") version "1.9.21"
    id("com.gradle.plugin-publish") version "1.2.1"
    `kotlin-dsl`
}

group = "io.github.isning.gradle.plugins.kn"
version = "0.1.3-0"

Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() && it.isFile }?.let { load(it.reader()) }
}.onEach { (key, value) ->
    if (key is String) ext[key] = value
}

if (!ext.has("gradle.publish.key")) ext["gradle.publish.key"] =
    System.getenv("GRADLE_PUBLISH_KEY")
if (!ext.has("gradle.publish.secret")) ext["gradle.publish.secret"] =
    System.getenv("GRADLE_PUBLISH_SECRET")

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("io.github.isning.gradle.plugins:CMakePlugin:0.1.2")
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