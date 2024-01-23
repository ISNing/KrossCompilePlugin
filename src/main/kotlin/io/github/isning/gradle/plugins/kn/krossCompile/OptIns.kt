package io.github.isning.gradle.plugins.kn.krossCompile

@RequiresOptIn(message = "This API should be managed by KrossCompile plugin in most scenarios.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class ManagedByKrossCompile
