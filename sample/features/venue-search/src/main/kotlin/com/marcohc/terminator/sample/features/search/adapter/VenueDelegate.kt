package com.marcohc.terminator.sample.features.search.adapter

import android.view.View
import com.marcohc.terminator.core.recycler.Delegate
import com.marcohc.terminator.core.recycler.DelegateConfig
import com.marcohc.terminator.sample.features.search.R
import kotlinx.android.synthetic.main.search_venue_item.view.search_venue_item_location_text
import kotlinx.android.synthetic.main.search_venue_item.view.search_venue_item_title_text

internal class VenueDelegate : Delegate<VenueItem.Venue> {

    override val delegateConfig = DelegateConfig.init<VenueItem.Venue>(R.layout.search_venue_item)

    override fun bind(view: View, item: VenueItem.Venue, childOnClickListener: View.OnClickListener) {
        view.search_venue_item_title_text.text = item.name
        view.search_venue_item_location_text.text = item.location
    }
}
