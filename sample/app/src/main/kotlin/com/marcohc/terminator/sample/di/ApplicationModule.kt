package com.marcohc.terminator.sample.di

import com.marcohc.terminator.sample.di.core.CoreModules
import com.marcohc.terminator.sample.di.features.FeaturesModule
import com.marcohc.terminator.sample.di.shared.SharedModules
import org.koin.core.module.Module

/**
 * This class contains the list of all modules of the app.
 *
 * Each module could have its own submodules.
 */
object ApplicationModule {

    val modules: List<Module>
        get() = mutableListOf<Module>()
            .apply {
                addAll(CoreModules.modules)
                addAll(SharedModules.modules)
                addAll(FeaturesModule.modules)
            }
}

