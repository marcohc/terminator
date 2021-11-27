package com.marcohc.terminator.core.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Interface for Adapter Delegate pattern
 */
interface AdapterDelegate<ItemModel : RecyclerItem> {

    fun onCreateViewHolder(parent: ViewGroup): BaseViewHolder<ItemModel>

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, items: List<ItemModel>)
}
