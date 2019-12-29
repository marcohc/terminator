package com.marcohc.terminator.sample.features.search.adapter

import com.marcohc.terminator.core.recycler.RecyclerItem

sealed class VenueItem : RecyclerItem {

    data class Venue(
            val id: String,
            val name: String,
            val location: String,
            val pictureUrl: String
    ) : VenueItem()
}
