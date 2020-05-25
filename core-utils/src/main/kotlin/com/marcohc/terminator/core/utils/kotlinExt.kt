package com.marcohc.terminator.core.utils

/**
 * Usage:
 *
 * when(sealedObject) {
 *     is OneType -> //
 *     is AnotherType -> //
 * }.exhaustive
 */
val <T> T.exhaustive: T
    get() = this

fun <T> unsafeLazy(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)
