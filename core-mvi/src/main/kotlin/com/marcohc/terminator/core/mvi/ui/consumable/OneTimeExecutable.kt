package com.marcohc.terminator.core.mvi.ui.consumable

import java.util.concurrent.atomic.AtomicReference

/**
 * Meant for UI actions which are executed only once
 */
class OneTimeExecutable private constructor(value: Unit? = null) {

    private val wrapperAtomicReference: AtomicReference<Unit?> = AtomicReference(value)

    fun execute(function: () -> Unit) {
        wrapperAtomicReference.getAndSet(null)?.let { function.invoke() }
    }

    fun isLoaded() = wrapperAtomicReference.get() != null

    companion object {
        fun load() = OneTimeExecutable(Unit)

        fun empty() = OneTimeExecutable()
    }
}
