package com.marcohc.terminator.core.recycler

import android.view.View

interface Delegate<ItemModel : RecyclerItem> {

    val delegateConfig: DelegateConfig<ItemModel>

    /**
     * Binds the view and the item model for this recycler view item.
     * Use {childOnClickListener} to hook child events into the main recycler view adapter click listener.
     */
    fun bind(
            view: View,
            item: ItemModel,
            childOnClickListener: View.OnClickListener
    )

}
