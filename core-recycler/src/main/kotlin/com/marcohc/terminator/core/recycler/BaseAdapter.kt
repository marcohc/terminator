package com.marcohc.terminator.core.recycler

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KClass

/**
 * Base adapter to be extended by any recycler view adapter.
 *
 * Use [setData] to set the items to the adapter. No need to call [notifyDataSetChanged]
 *
 * Use [setOnItemClickListener] for item click and child clicks
 */
abstract class BaseAdapter<ItemModel : RecyclerItem> : RecyclerView.Adapter<BaseViewHolder<*>>() {

    private var items = emptyList<ItemModel>()
    private val delegates: SparseArray<AdapterDelegate<ItemModel>>
    private val itemModelAndViewHoldersMap: Map<KClass<*>, Int>
    private var itemClickListenerFun: ((view: View, position: Int, item: ItemModel) -> Unit)? = null
    private var itemLongClickListenerFun: ((view: View, position: Int, item: ItemModel) -> Unit)? =
        null

    init {
        val supportedViewHolderInfoList = this.getDelegatesList()
        delegates = SparseArray(supportedViewHolderInfoList.size)
        itemModelAndViewHoldersMap = HashMap()

        supportedViewHolderInfoList.forEachIndexed { index, viewHolderInfo ->
            delegates.put(index, TypedAdapterDelegate(viewHolderInfo))
            itemModelAndViewHoldersMap[viewHolderInfo.delegateConfig.itemModelClass] = index
        }
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return delegates[viewType]?.onCreateViewHolder(parent)
            ?: throw IllegalStateException("No delegate for $viewType viewType")
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val adapterDelegate = delegates[getItemViewType(position)]

        val onClickListener = View.OnClickListener { view ->
            val index = holder.adapterPosition
            if (RecyclerView.NO_POSITION != index) {
                itemClickListenerFun?.invoke(view, index, items[index])
            }
        }

        val onLongClickListener = View.OnLongClickListener { view ->
            val index = holder.adapterPosition
            if (RecyclerView.NO_POSITION != index) {
                itemLongClickListenerFun?.invoke(view, index, items[index])
                return@OnLongClickListener true
            }
            false
        }

        holder.itemView.setOnClickListener(onClickListener)
        holder.setChildOnClickListener(onClickListener)

        holder.itemView.setOnLongClickListener(onLongClickListener)
        holder.setChildOnLongClickListener(onLongClickListener)

        adapterDelegate.onBindViewHolder(holder, position, items)
    }

    override fun getItemViewType(position: Int): Int {
        return itemModelAndViewHoldersMap[items[position]::class]
            ?: throw IllegalStateException("This item doesn't have any delegate associated")
    }

    abstract fun getDelegatesList(): List<Delegate<ItemModel>>

    fun getData() = items

    fun setData(data: List<ItemModel>) {
        val result = DiffUtil.calculateDiff(CommonDiffUtilCallback(items, data))
        this.items = data
        result.dispatchUpdatesTo(this)
    }

    fun setOnItemClickListener(itemClickListenerFun: (view: View, position: Int, item: ItemModel) -> Unit) {
        this.itemClickListenerFun = itemClickListenerFun
    }

    fun setOnItemLongClickListener(itemLongClickListenerFun: (view: View, position: Int, item: ItemModel) -> Unit) {
        this.itemLongClickListenerFun = itemLongClickListenerFun
    }
}
