package com.marcohc.terminator.core.recycler

import androidx.recyclerview.widget.DiffUtil

/**
 * Class used to ensure equality of items in the list and update only the necessary ones
 */
class CommonDiffUtilCallback<ItemModel : RecyclerItem>(
        private val oldItems: List<ItemModel>,
        private val newItems: List<ItemModel>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]
        val sameClass = oldItem::class == newItem::class
        return if (!sameClass) false
        else {
            areContentsTheSame(oldItemPosition, newItemPosition)
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }
}
