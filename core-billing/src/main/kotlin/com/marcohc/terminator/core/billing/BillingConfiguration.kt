package com.marcohc.terminator.core.billing

data class BillingConfiguration(
    val debug: Boolean,
    val productIdsList: List<String>
)
