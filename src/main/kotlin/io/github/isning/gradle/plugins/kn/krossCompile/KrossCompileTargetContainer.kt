package io.github.isning.gradle.plugins.kn.krossCompile

import org.gradle.api.InvalidUserCodeException
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectCollection

interface KrossCompileTargetFactory<T : KrossCompileTarget<*>> : Named {
    val factoryName: String
    override fun getName(): String {
        return factoryName
    }

    fun create(name: String): T?
}

interface KrossCompileTargetContainer {
    val rawTargets: NamedDomainObjectCollection<KrossCompileTarget<*>>
}

interface KrossCompileTargetContainerWithFactories : KrossCompileTargetContainer {
    val factories: NamedDomainObjectCollection<KrossCompileTargetFactory<*>>
}

internal inline fun <reified T : KrossCompileTarget<*>> KrossCompileTargetContainerWithFactories.configureOrCreate(
    targetName: String,
    targetFactory: KrossCompileTargetFactory<T>,
    configure: T.() -> Unit,
): T {
    when (val existingTarget = rawTargets.findByName(targetName)) {
        null -> {
            val newTarget = targetFactory.create(targetName)
            if (newTarget != null) {
                rawTargets.add(newTarget)
                configure(newTarget)
                return newTarget
            } else error("The target '$targetName' could not be created with the '${targetFactory.factoryName}' factory")
        }

        is T -> {
            configure(existingTarget)
            return existingTarget
        }

        else -> {
            throw InvalidUserCodeException(
                "The target '$targetName' already exists, but it was not created with the '${targetFactory.factoryName}' factory. " +
                        "To configure it, access it by name in `targets`"
            )
        }
    }
}
