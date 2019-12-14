package com.marcohc.terminator.core.koin

import org.koin.core.module.Module

/**
 * Implement this interface for the module of a single feature.
 */
interface FeatureModule {

    /**
     * The unique scopeId
     */
    val scopeId: String

    /**
     * The Koin module
     */
    val module: Module

}
