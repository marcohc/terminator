package com.marcohc.terminator.sample.features.search

import com.marcohc.terminator.core.koin.FeatureModule
import com.marcohc.terminator.core.mvi.ext.declareActivityInteractor
import com.marcohc.terminator.core.mvi.ext.declareFactoryActivityRouter
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

object SearchModule : FeatureModule {

    override val scopeId: String = "SearchModule"

    override val module = module {

        declareFactoryActivityRouter(scopeId = scopeId) { navigationExecutor ->
            SearchRouter(
                navigationExecutor = navigationExecutor,
                applicationNavigator = get()
            )
        }

        declareActivityInteractor(scopeId = scopeId) {
            SearchInteractor(
                getVenuesByCityUseCase = GetVenuesByCityUseCase(
                    connectionManager = get(),
                    venueRepository = get(),
                    scheduler = get()
                ),
                router = get(),
                analytics = SearchAnalytics(),
                resourceProvider = SearchResourceProvider(androidApplication()),
                connectionManager = get()
            )
        }
    }

}
