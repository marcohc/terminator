package com.marcohc.terminator.sample.features.search

import com.marcohc.terminator.core.mvi.ui.consumable.Consumable
import com.marcohc.terminator.core.mvi.ui.consumable.OneTimeExecutable
import com.marcohc.terminator.sample.features.search.adapter.VenueItem

sealed class SearchIntention {
    object Initial : SearchIntention()
    data class Search(val city: String) : SearchIntention()
    data class ItemClick(val item: VenueItem.Venue) : SearchIntention()
}

internal sealed class SearchResult {
    object Loading : SearchResult()
    data class Render(
            val statusText: String = "",
            val items: List<VenueItem> = emptyList()
    ) : SearchResult()

    class ShowToast(val connected: Boolean) : SearchResult()
}

data class SearchState(
        val loading: Boolean = false,
        val items: List<VenueItem> = emptyList(),
        val status: String = "",
        val connected: Consumable<Boolean> = Consumable()
)
