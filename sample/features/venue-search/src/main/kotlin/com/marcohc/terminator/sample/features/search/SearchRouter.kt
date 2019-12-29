package com.marcohc.terminator.sample.features.search

import com.marcohc.terminator.core.mvi.ui.navigation.ActivityNavigationExecutor
import com.marcohc.terminator.sample.features.search.adapter.VenueItem

internal class SearchRouter(
        private val navigationExecutor: ActivityNavigationExecutor,
        private val applicationNavigator: SearchNavigator
) {

    fun goToVenueDetails(item: VenueItem.Venue) = navigationExecutor.executeCompletable { applicationNavigator.goToVenue(it, REQUEST_CODE_DETAILS, item.id) }

    companion object {
        private const val REQUEST_CODE_DETAILS = 1234
    }

}
