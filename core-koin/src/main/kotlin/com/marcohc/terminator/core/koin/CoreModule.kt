package com.marcohc.terminator.core.koin

import org.koin.core.module.Module

/**
 * Implement this interface for a single core module.
 */
interface CoreModule {

    /**
     * The Koin module
     */
    val module: Module

}
