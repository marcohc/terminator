package com.marcohc.terminator.core.billing.data.models

class Subscription(
    val productId: String,
    val type: String,
    val price: Double,
    val priceFormatted: String
)
