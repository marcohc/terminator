package com.marcohc.terminator.core.billing.data.api

import android.app.Activity
import com.android.billingclient.api.Purchase
import com.marcohc.terminator.core.billing.data.models.Subscription
import io.reactivex.Single

internal interface BillingApi {
    fun connect()

    fun disconnect()

    fun showProductCheckout(
        activity: Activity,
        productId: String
    ): Single<GoogleBillingResponse<List<Purchase>>>

    fun getSubscriptions(): Single<GoogleBillingResponse<List<Subscription>>>
}
