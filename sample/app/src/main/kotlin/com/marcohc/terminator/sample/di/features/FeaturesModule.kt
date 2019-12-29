package com.marcohc.terminator.sample.di.features

import com.marcohc.terminator.sample.features.detail.DetailModule
import com.marcohc.terminator.sample.features.search.SearchModule
import org.koin.core.module.Module

object FeaturesModule {

    val modules: List<Module>
        get() = mutableListOf<Module>()
            .apply {
                add(SearchModule.module)
                add(DetailModule.module)
            }
}
