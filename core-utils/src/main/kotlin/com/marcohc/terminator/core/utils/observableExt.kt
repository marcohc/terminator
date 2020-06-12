@file:Suppress("unused")

package com.marcohc.terminator.core.utils

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction

fun <T> Observable<T>.toSingle(): Single<T> = take(1).singleOrError()

fun <T> T.observableJust() = Observable.just(this)

fun <A, B> observableCombineLatest(streamA: Observable<A>, streamB: Observable<B>): Observable<Pair<A, B>> = Observable
    .combineLatest(
        streamA,
        streamB,
        BiFunction { a, b -> a to b }
    )

fun <A, B> observableZip(streamA: Observable<A>, streamB: Observable<B>): Observable<Pair<A, B>> = Observable
    .zip(
        streamA,
        streamB,
        BiFunction { a, b -> a to b }
    )
