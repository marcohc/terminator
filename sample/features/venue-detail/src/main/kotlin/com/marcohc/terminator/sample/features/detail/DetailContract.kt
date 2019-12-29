package com.marcohc.terminator.sample.features.detail

import com.marcohc.terminator.sample.data.model.Venue

sealed class DetailIntention {
    data class Initial(val venueId: String) : DetailIntention()
}

internal sealed class DetailAction {
    object Load : DetailAction()
    data class Render(val items: Venue) : DetailAction()
    data class Error(val errorText: String) : DetailAction()
}

sealed class DetailState {
    object Loading : DetailState()
    data class Data(val venue: Venue) : DetailState()
    data class Error(val errorText: String) : DetailState()
}
