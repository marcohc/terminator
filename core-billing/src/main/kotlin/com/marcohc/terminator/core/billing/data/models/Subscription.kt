package com.marcohc.terminator.core.billing.data.models

import com.android.billingclient.api.ProductDetails

class Subscription(
    val productId: String,
    val type: String,
    val price: Double,
    val priceFormatted: String,
    val productDetails: ProductDetails
)
