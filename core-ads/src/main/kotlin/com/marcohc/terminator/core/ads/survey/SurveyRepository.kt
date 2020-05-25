package com.marcohc.terminator.core.ads.survey

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import com.marcohc.terminator.core.ads.AdsConstants
import io.reactivex.Completable
import io.reactivex.Observable
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

interface SurveyRepository {

    fun observe(): Observable<SurveyEvent>

    fun getLastEvent(): SurveyEvent

    @MainThread
    fun openSurvey(): Completable

    companion object {
        fun Scope.factorySurveyRepository(activity: AppCompatActivity): SurveyRepository = SurveyRepositoryImpl(
            activity = activity,
            debug = get(named(AdsConstants.SURVEY_DEBUG)),
            apiKey = get(named(AdsConstants.SURVEY_API_KEY))
        )

        fun factoryStubSurveyRepository(): SurveyRepository = object : SurveyRepository {
            override fun observe() = Observable.never<SurveyEvent>()
            override fun getLastEvent(): SurveyEvent = SurveyEvent.NotLoadedYet
            override fun openSurvey() = Completable.complete()
        }
    }
}
