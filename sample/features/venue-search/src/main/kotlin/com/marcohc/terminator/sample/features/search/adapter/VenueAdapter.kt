package com.marcohc.terminator.sample.features.search.adapter

import com.marcohc.terminator.core.recycler.BaseAdapter
import com.marcohc.terminator.core.recycler.Delegate

internal class VenueAdapter : BaseAdapter<VenueItem>() {

    @Suppress("unchecked_cast")
    override fun getDelegatesList(): List<Delegate<VenueItem>> = listOf(
        VenueDelegate()
    ) as List<Delegate<VenueItem>>
}

