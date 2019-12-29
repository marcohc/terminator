package com.marcohc.terminator.sample.features.detail

import com.marcohc.terminator.core.koin.FeatureModule
import com.marcohc.terminator.core.mvi.ext.declareActivityInteractor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

object DetailModule : FeatureModule {

    override val scopeId = "DetailModule"

    override val module = module {
        factory { DetailAnalytics() }

        factory {
            GetVenueByIdUseCase(
                connectionManager = get(),
                venueRepository = get(),
                scheduler = get()
            )
        }

        factory { DetailResourceProvider(context = androidApplication()) }

        declareActivityInteractor(scopeId = scopeId) {
            DetailInteractor(
                getVenueByIdUseCase = get(),
                analytics = get(),
                resourceProvider = get()
            )
        }
    }

}
