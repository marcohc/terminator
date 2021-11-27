package com.marcohc.terminator.core.recycler

import kotlin.reflect.KClass

/**
 * Configuration class for ViewHolder.
 */
data class DelegateConfig<ItemModel : RecyclerItem>(
    val layoutId: Int,
    val itemModelClass: KClass<ItemModel>
) {
    companion object {
        inline fun <reified T : RecyclerItem> init(layoutId: Int): DelegateConfig<T> = DelegateConfig(layoutId, T::class)
    }
}
