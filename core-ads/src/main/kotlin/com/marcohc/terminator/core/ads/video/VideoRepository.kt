package com.marcohc.terminator.core.ads.video

import androidx.annotation.MainThread
import io.reactivex.Completable
import io.reactivex.Observable

interface VideoRepository {

    @MainThread
    fun loadVideo(): Completable

    fun observe(): Observable<VideoEvent>

    fun getLastEvent(): VideoEvent

    @MainThread
    fun openVideo(): Completable

}
