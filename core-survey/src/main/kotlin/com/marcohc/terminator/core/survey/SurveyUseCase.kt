package com.marcohc.terminator.core.survey

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.marcohc.terminator.core.mvi.ext.getOrCreateFromParentScope
import com.marcohc.terminator.core.utils.toObservableDefault
import com.pollfish.classes.SurveyInfo
import com.pollfish.constants.SurveyFormat
import com.pollfish.constants.UserProperties
import com.pollfish.main.PollFish
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import timber.log.Timber
import java.util.Locale

interface SurveyUseCase {

    fun observe(): Observable<SurveyEvent>

    fun observeAndTrack(): Observable<SurveyEvent>

    fun getLastEvent(): SurveyEvent

    fun logShowSurveyClick(): Completable

    @MainThread
    fun show(): Completable

    companion object {

        fun Scope.getOrCreateScopedSurveyUseCase(
                analyticsScopeId: String,
                activity: AppCompatActivity
        ): SurveyUseCase = getOrCreateFromParentScope(SurveyModule.scopeId) { factorySurveyUseCase(analyticsScopeId, activity) }

        fun Scope.factorySurveyUseCase(
                analyticsScopeId: String,
                activity: AppCompatActivity
        ): SurveyUseCase = SurveyUseCaseImpl(
            activity = activity,
            analytics = SurveyAnalyticsImpl(
                analytics = get(),
                scopeId = analyticsScopeId
            ),
            debug = get(named(SurveyConstants.SURVEY_DEBUG)),
            apiKey = get(named(SurveyConstants.SURVEY_API_KEY))
        )

        fun factoryStubSurveyUseCase(): SurveyUseCase = object : SurveyUseCase {
            override fun observe() = Observable.never<SurveyEvent>()
            override fun observeAndTrack() = Observable.never<SurveyEvent>()
            override fun getLastEvent(): SurveyEvent = SurveyEvent.NotLoadedYet
            override fun logShowSurveyClick() = Completable.complete()
            override fun show() = Completable.complete()
        }
    }
}

internal class SurveyUseCaseImpl(
        private val activity: AppCompatActivity,
        private val analytics: SurveyAnalytics,
        private val debug: Boolean,
        private val apiKey: String
) : SurveyUseCase,
    LifecycleObserver {

    private val subject = BehaviorSubject.createDefault<SurveyEvent>(SurveyEvent.NotLoadedYet)
    private var lastPrice = 0.0

    init {
        activity.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        loadSurvey()
    }

    override fun logShowSurveyClick() = Completable.fromAction { analytics.logClick() }

    override fun observe(): Observable<SurveyEvent> = subject.hide()

    override fun observeAndTrack(): Observable<SurveyEvent> = observe()
        .flatMap { event -> analytics.logEvent(event).toObservableDefault(event) }

    override fun getLastEvent() = requireNotNull(subject.value) { "This subject must contain always a value" }

    @MainThread
    override fun show() = Completable.fromAction {
        if (PollFish.isPollfishPresent()) {
            PollFish.show()
        }
    }

    private fun loadSurvey() {
        PollFish
            .initWith(
                activity,
                PollFish.ParamsBuilder(apiKey)
                    .releaseMode(!debug)
                    .rewardMode(true)
                    .offerWallMode(false)
                    .surveyFormat(SurveyFormat.PLAYFUL)
                    .pollfishSurveyNotAvailableListener {
                        Timber.v("SurveyEvent.NotAvailable")
                        subject.onNext(SurveyEvent.NotAvailable)
                    }
                    .pollfishReceivedSurveyListener { surveyInfo: SurveyInfo ->
                        lastPrice = surveyInfo.surveyCPA / 100.0
                        Timber.v("SurveyEvent.Received: $surveyInfo")
                        subject.onNext(SurveyEvent.Received(lastPrice))
                    }
                    .pollfishOpenedListener {
                        Timber.v("SurveyEvent.Opened")
                        subject.onNext(SurveyEvent.Opened(lastPrice))
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
                        subject.onNext(SurveyEvent.NotAvailable)
                        loadSurvey()
                    }
                    .pollfishCompletedSurveyListener { surveyInfo: SurveyInfo ->
                        val price = surveyInfo.surveyCPA / 100.0
                        Timber.v("SurveyEvent.Received: $surveyInfo")
                        subject.onNext(SurveyEvent.Rewarded(price))
                    }
                    .userProperties(
                        // TODO: This should be somehow extracted out but without dragging the Pollfish dependency
                        UserProperties().setSpokenLanguages(
                            if (userHasLocaleFromSpain()) {
                                UserProperties.SpokenLanguages.SPANISH
                            } else {
                                UserProperties.SpokenLanguages.ENGLISH
                            }
                        )
                    )
                    .build()
            )
        PollFish.hide()
    }

    private fun userHasLocaleFromSpain() = listOf("es", "ca", "gl", "eu").contains(Locale.getDefault().language)

}
