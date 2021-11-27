package com.marcohc.terminator.core.koin

import org.koin.core.module.Module

/**
 * Implement this interface for the module of a coordinator
 */
abstract class CoordinatorModule {

    /**
     * The unique scopeId
     */
    abstract val scopeId: String

    /**
     * The inner Coordinator Koin module
     */
    protected abstract val module: Module

    /**
     * The list of feature's modules under the roof of this Coordinator
     */
    abstract val modules: List<Module>
}
