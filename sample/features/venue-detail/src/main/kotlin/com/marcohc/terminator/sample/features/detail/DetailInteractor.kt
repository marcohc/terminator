package com.marcohc.terminator.sample.features.detail

import com.marcohc.terminator.core.mvi.domain.MviBaseInteractor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

internal class DetailInteractor(
        private val getVenueByIdUseCase: GetVenueByIdUseCase,
        private val analytics: DetailAnalytics,
        private val resourceProvider: DetailResourceProvider
) : MviBaseInteractor<DetailIntention, DetailAction, DetailState>(defaultState = DetailState.Loading) {

    private lateinit var venueId: String

    override fun intentionToAction(): (DetailIntention) -> Observable<out DetailAction> = { intention ->
        when (intention) {
            is DetailIntention.Initial -> initial(intention.venueId).toObservable()
        }
    }

    private fun initial(venueId: String): Single<DetailAction> {
        return Completable.fromAction { analytics.logItemView() }
            .andThen(getVenueByIdUseCase.execute(venueId))
            .map<DetailAction> { DetailAction.Render(it) }
            .onErrorReturn { DetailAction.Error(resourceProvider.getErrorMessage()) }
    }

    override fun actionToState(): (DetailState, DetailAction) -> DetailState = { state, action ->
        when (state) {
            DetailState.Loading -> {
                when (action) {
                    is DetailAction.Render -> DetailState.Data(venue = action.items)
                    is DetailAction.Error -> DetailState.Error(errorText = action.errorText)
                    else -> null
                }
            }
            is DetailState.Data -> {
                when (action) {
                    is DetailAction.Load -> DetailState.Loading
                    else -> null
                }
            }
            is DetailState.Error -> {
                when (action) {
                    is DetailAction.Load -> DetailState.Loading
                    else -> null
                }
            }
        } ?: throw IllegalStateException("From $state you cannot execute $action")
    }

}
