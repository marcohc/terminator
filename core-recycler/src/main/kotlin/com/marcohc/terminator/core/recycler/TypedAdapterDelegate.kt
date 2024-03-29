package com.marcohc.terminator.core.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Implementation for Adapter Delegate pattern
 */
class TypedAdapterDelegate<ItemModel : RecyclerItem>(
    private val delegate: Delegate<ItemModel>
) : AdapterDelegate<ItemModel> {

    override fun onCreateViewHolder(parent: ViewGroup): BaseViewHolder<ItemModel> {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(delegate.delegateConfig.layoutId, parent, false)
        return object : BaseViewHolder<ItemModel>(itemView) {

            override val delegateConfig = delegate.delegateConfig
            lateinit var childClickListener: View.OnClickListener
            lateinit var childLongClickListener: View.OnLongClickListener

            override fun setChildOnClickListener(onClickListener: View.OnClickListener) {
                childClickListener = onClickListener
            }

            override fun setChildOnLongClickListener(onLongClickListener: View.OnLongClickListener) {
                childLongClickListener = onLongClickListener
            }

            override fun bind(item: ItemModel) {
                delegate.bind(
                    itemView,
                    item,
                    childClickListener,
                    childLongClickListener
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        items: List<ItemModel>
    ) {
        val itemModel = items[position] as? ItemModel
        val data = if (itemModel is ItemModel) {
            itemModel
        } else {
            throw IllegalStateException(
                "Incorrect data for position $position." +
                    " Was ${items[position]}. Check your items list."
            )
        }
        (holder as BaseViewHolder<ItemModel>).bind(data)
    }
}
