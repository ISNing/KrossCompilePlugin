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

package io.github.isning.gradle.plugins.kn.krossCompile.utils

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.internal.provider.AbstractMinimalProvider
import org.gradle.api.internal.provider.ValueSupplier
import org.gradle.api.provider.Provider

fun <T> NamedDomainObjectContainer<T>.delegateItemsTo(
    targetContainer: MutableSet<in T>,
) = delegateItemsTransformedTo(targetContainer) { it }

fun <T1, T2> NamedDomainObjectContainer<T1>.delegateItemsTransformedTo(
    targetContainer: MutableSet<in T2>,
    transform: (T1) -> T2,
) {
    this.whenObjectAdded {
        targetContainer.add(transform(this))
    }
    this.whenObjectRemoved {
        targetContainer.remove(transform(this))
    }
}

class SettableProvider<T : Any> : AbstractMinimalProvider<T>() {
    private var value: T? = null
    private var provider: Provider<T>? = null

    fun set(provider: Provider<T>) {
        this.provider = provider
    }

    fun set(value: T) {
        this.value = value
    }

    override fun getType(): Class<T>? {
        return value?.javaClass
    }

    override fun calculateOwnValue(consumer: ValueSupplier.ValueConsumer): ValueSupplier.Value<out T> {
        return ValueSupplier.Value.ofNullable(value ?: provider?.orNull)
    }
}