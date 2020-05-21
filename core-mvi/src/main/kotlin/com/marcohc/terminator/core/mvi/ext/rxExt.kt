package com.marcohc.terminator.core.mvi.ext

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.Subject

fun <T> T.singleJust() = Single.just(this)

fun <T> T.observableJust() = Observable.just(this)

fun <A, B> singleZip(streamA: Single<A>, streamB: Single<B>): Single<Pair<A, B>> = Single
    .zip(
        streamA,
        streamB,
        BiFunction { a, b -> a to b }
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
        Function3 { a, b, c -> Triple(a, b, c) }
    )

fun <A, B> observableCombineLatest(streamA: Observable<A>, streamB: Observable<B>): Observable<Pair<A, B>> = Observable
    .combineLatest(
        streamA,
        streamB,
        BiFunction { a, b -> a to b }
    )

fun <T> Subject<T>.onNextCompletable(event: T) = Completable.fromAction { this.onNext(event) }

fun <T> Subject<T>.toDisposableObserver(): DisposableObserver<T> = DisposableSubject(this)

private class DisposableSubject<T>(private val subject: Subject<T>) : DisposableObserver<T>() {

    override fun onNext(next: T) {
        subject.onNext(next)
    }

    override fun onComplete() {
        subject.onComplete()
    }

    override fun onError(e: Throwable) {
        subject.onError(e)
    }
}
