@file:Suppress("NOTHING_TO_INLINE")

package com.marcohc.terminator.core.mvi.test

import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

inline fun <T> Observable<T>.mockValue(value: T) {
    whenever(this).thenReturn(Observable.just(value))
}

inline fun <T> Observable<T>?.mockNever() {
    whenever(this).thenReturn(Observable.never())
}

inline fun <T> Observable<T>.mockError(value: Throwable = Throwable()) {
    whenever(this).thenReturn(Observable.error(value))
}

inline fun <T> Single<T>.mockValue(value: T) {
    whenever(this).thenReturn(Single.just(value))
}

inline fun <T> Single<T>?.mockNever() {
    whenever(this).thenReturn(Single.never())
}

inline fun <T> Single<T>.mockError(value: Throwable = Throwable()) {
    whenever(this).thenReturn(Single.error(value))
}

inline fun <T> Maybe<T>.mockValue(value: T) {
    whenever(this).thenReturn(Maybe.just(value))
}

inline fun <T> Maybe<T>?.mockNever() {
    whenever(this).thenReturn(Maybe.never())
}

inline fun <T> Maybe<T>.mockError(value: Throwable = Throwable()) {
    whenever(this).thenReturn(Maybe.error(value))
}

inline fun Completable.mockError(value: Throwable = Throwable()) {
    whenever(this).thenReturn(Completable.error(value))
}

inline fun Completable?.mockNever() {
    whenever(this).thenReturn(Completable.never())
}

inline fun Completable?.mockComplete() {
    whenever(this).thenReturn(Completable.complete())
}
