package com.marcohc.terminator.core.recycler

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Base class to be extended by any ViewHolder
 */
abstract class BaseViewHolder<ItemModel : RecyclerItem>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract val delegateConfig: DelegateConfig<ItemModel>

    abstract fun bind(item: ItemModel)

    abstract fun setChildOnClickListener(onClickListener: View.OnClickListener)

}
