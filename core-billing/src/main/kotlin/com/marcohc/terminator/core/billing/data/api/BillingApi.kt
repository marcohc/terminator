package com.marcohc.terminator.core.billing.data.api

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import io.reactivex.Single

internal interface BillingApi {
    fun connect()

    fun disconnect()

    fun showProductCheckout(
        activity: Activity,
        productDetails: ProductDetails
    ): Single<GoogleBillingResponse<List<Purchase>>>

    fun getSubscriptions(): Single<GoogleBillingResponse<List<ProductDetails>>>
}
