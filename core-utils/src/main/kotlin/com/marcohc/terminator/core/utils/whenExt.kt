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
