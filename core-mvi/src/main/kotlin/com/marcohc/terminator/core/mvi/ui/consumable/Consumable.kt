package com.marcohc.terminator.core.mvi.ui.consumable

import java.util.concurrent.atomic.AtomicReference

/**
 * Class which contains an atomic reference of an object and exposes its value only once though consume method
 */
class Consumable<T>(value: T? = null) {

    private val wrapperAtomicReference: AtomicReference<T?> = AtomicReference(value)

    fun consume(): T? {
        return wrapperAtomicReference.getAndSet(null)
    }

}
