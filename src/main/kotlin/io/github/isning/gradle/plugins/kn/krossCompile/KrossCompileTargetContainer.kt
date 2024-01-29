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
