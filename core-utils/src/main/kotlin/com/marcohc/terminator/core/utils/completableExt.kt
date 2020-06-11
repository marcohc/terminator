@file:Suppress("unused")

package com.marcohc.terminator.core.utils

import io.reactivex.Completable
import io.reactivex.Observable

fun <T> Completable.toObservableDefault(value: T): Observable<T> = toSingleDefault(value).toObservable()

fun Completable.andThenCompletable(function: () -> Unit) = Completable.fromAction { function.invoke() }
