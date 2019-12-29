package com.marcohc.terminator.sample.features.search

import com.marcohc.terminator.core.mvi.domain.MviBaseInteractor
import com.marcohc.terminator.core.mvi.ui.consumable.Consumable
import com.marcohc.terminator.sample.data.model.Venue
import com.marcohc.terminator.sample.data.repositories.ConnectionManager
import com.marcohc.terminator.sample.data.repositories.LocationUnknownException
import com.marcohc.terminator.sample.features.search.adapter.VenueItem
import io.reactivex.Completable
import io.reactivex.Observable

internal class SearchInteractor(
        private val getVenuesByCityUseCase: GetVenuesByCityUseCase,
        private val router: SearchRouter,
        private val analytics: SearchAnalytics,
        private val resourceProvider: SearchResourceProvider,
        private val connectionManager: ConnectionManager
) : MviBaseInteractor<SearchIntention, SearchResult, SearchState>(defaultState = SearchState()) {

    override fun intentionToAction(): (SearchIntention) -> Observable<out SearchResult> = { intention ->
        when (intention) {
            is SearchIntention.Initial -> initial()
            is SearchIntention.Search -> search(intention.city)
            is SearchIntention.ItemClick -> itemClick(intention.item).toObservable()
        }
    }

    private fun initial(): Observable<SearchResult.ShowToast> {
        return connectionManager.observeConnection()
            .map { SearchResult.ShowToast(it) }
    }

    fun search(city: String): Observable<SearchResult> {
        return Completable.fromAction { analytics.logSearchClick() }
            .andThen(getVenuesByCityUseCase.execute(city)).toObservable()
            .map<SearchResult> { items ->
                if (items.isEmpty()) {
                    SearchResult.Render(statusText = resourceProvider.getNoItemsString(city))
                } else {
                    SearchResult.Render(items = mapModelsToItems(items))
                }
            }
            .onErrorReturn {
                SearchResult.Render(
                    statusText = if (it is LocationUnknownException) {
                        resourceProvider.getLocationUnknownText()
                    } else {
                        resourceProvider.getGeneralErrorText()
                    }
                )
            }
            .startWith(SearchResult.Loading)
    }

    private fun itemClick(item: VenueItem.Venue): Completable {
        return Completable.fromAction { analytics.logItemClick() }
            .andThen(router.goToVenueDetails(item))
    }

    private fun mapModelsToItems(models: List<Venue>): List<VenueItem> {
        return models.map {
            VenueItem.Venue(
                it.id,
                it.name,
                it.location,
                it.pictureUrl
            )
        }
    }

    override fun actionToState(): (SearchState, SearchResult) -> SearchState = { state, action ->
        with(state) {
            when (action) {
                is SearchResult.Loading -> copy(loading = true)
                is SearchResult.ShowToast -> copy(connected = Consumable(action.connected))
                is SearchResult.Render -> copy(
                    loading = false,
                    items = action.items,
                    status = action.statusText
                )
            }
        }

    }
}
