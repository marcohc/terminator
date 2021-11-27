package com.marcohc.terminator.core.mvi.ui

/**
 * Data class which is used by Mvi views to set up their basic stuff
 */
data class MviConfig(
    val scopeId: String,
    val layoutId: Int,
    val mviConfigType: MviConfigType = MviConfigType.NO_SCOPE
)
