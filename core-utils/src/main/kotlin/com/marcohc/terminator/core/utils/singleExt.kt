@file:Suppress("unused")

package com.marcohc.terminator.core.utils

import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3

fun <T> T.singleJust() = Single.just(this)

fun <A, B> singleZip(streamA: Single<A>, streamB: Single<B>): Single<Pair<A, B>> = Single
    .zip(
        streamA,
        streamB,
        { a: A, b: B -> a to b }
    )

fun <A, B, C> singleZip(
    streamA: Single<A>,
    streamB: Single<B>,
    streamC: Single<C>
): Single<Triple<A, B, C>> = Single
    .zip(
        streamA,
        streamB,
        streamC,
        { a: A, b: B, c: C -> Triple(a, b, c) }
    )
