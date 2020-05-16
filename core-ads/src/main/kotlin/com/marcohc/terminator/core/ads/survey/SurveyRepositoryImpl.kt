package com.marcohc.terminator.core.ads.survey

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.pollfish.classes.SurveyInfo
import com.pollfish.main.PollFish
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

internal class SurveyRepositoryImpl(
        private val activity: AppCompatActivity,
        private val debug: Boolean,
        private val apiKey: String
) : SurveyRepository,
    LifecycleObserver {

    private val subject = BehaviorSubject.createDefault<SurveyEvent>(SurveyEvent.NotLoadedYet)

    init {
        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {

        val paramsBuilder = PollFish.ParamsBuilder(apiKey)
            .releaseMode(!debug)
            .rewardMode(true)
            .pollfishSurveyNotAvailableListener {
                Timber.v("SurveyEvent.NotAvailable")
                subject.onNext(SurveyEvent.NotAvailable)
            }
            .pollfishReceivedSurveyListener { surveyInfo: SurveyInfo ->
                val price = surveyInfo.surveyCPA / 100.0
                Timber.v("SurveyEvent.Received: $surveyInfo")
                subject.onNext(SurveyEvent.Received(price))
            }
            .pollfishOpenedListener {
                Timber.v("SurveyEvent.Opened")
                subject.onNext(SurveyEvent.Opened)
            }
            .pollfishUserRejectedSurveyListener {
                Timber.v("SurveyEvent.UserRejected")
                subject.onNext(SurveyEvent.UserRejected)
            }
            .pollfishUserNotEligibleListener {
                Timber.v("SurveyEvent.NotEligible")
                subject.onNext(SurveyEvent.NotEligible)
            }
            .pollfishClosedListener {
                Timber.v("SurveyEvent.Closed")
                subject.onNext(SurveyEvent.Closed)
            }
            .pollfishCompletedSurveyListener { surveyInfo: SurveyInfo ->
                val price = surveyInfo.surveyCPA / 100.0
                Timber.v("SurveyEvent.Received: $surveyInfo")
                subject.onNext(SurveyEvent.Rewarded(price))
            }
            .build()

        PollFish.initWith(activity, paramsBuilder)
        PollFish.hide()
    }

    override fun loadSurvey() = Completable.fromAction {
        // TODO: Implement reload
    }

    override fun observe(): Observable<SurveyEvent> {
        return subject.hide()
    }

    override fun getLastEvent() = requireNotNull(subject.value) { "This subject must contain always a value" }

    @MainThread
    override fun openSurvey() = Completable.fromAction {
        if (PollFish.isPollfishPresent()) {
            PollFish.show()
        }
    }

}
