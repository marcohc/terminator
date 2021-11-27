package com.marcohc.terminator.core.mvi.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory<out V : ViewModel>(private val viewModelFactoryFunction: () -> V) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return viewModelFactoryFunction.invoke() as T
    }
}
