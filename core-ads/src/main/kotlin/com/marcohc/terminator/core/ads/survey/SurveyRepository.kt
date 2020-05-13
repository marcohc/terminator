package com.marcohc.terminator.core.ads.survey

import androidx.annotation.MainThread
import io.reactivex.Completable
import io.reactivex.Observable

interface SurveyRepository {

    @MainThread
    fun loadSurvey(): Completable

    fun observe(): Observable<SurveyEvent>

    fun getLastEvent(): SurveyEvent

    @MainThread
    fun openSurvey(): Completable

}
