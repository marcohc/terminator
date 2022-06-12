package com.marcohc.terminator.core.billing.data.repositories

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.marcohc.terminator.core.billing.data.api.BillingApi
import com.marcohc.terminator.core.billing.data.api.GoogleBillingResponse
import com.marcohc.terminator.core.billing.data.models.Subscription
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single

interface BillingRepository {
    fun connect()

    fun disconnect()

    fun getSubscriptions(): Single<List<Subscription>>

    fun showProductCheckout(activity: Activity, productDetail: ProductDetails): Completable
}

internal class BillingRepositoryImpl(
    private val api: BillingApi,
    private val scheduler: Scheduler
) : BillingRepository {

    private var localProductDetails: List<Subscription> = emptyList()

    override fun connect() {
        api.connect()
    }

    override fun disconnect() {
        api.disconnect()
    }

    override fun getSubscriptions(): Single<List<Subscription>> {
        return if (localProductDetails.isEmpty()) {
            api.getSubscriptions()
                .map { response ->
                    when (response) {
                        is GoogleBillingResponse.Success -> {
                            localProductDetails = response.result.map { productDetails ->
                                val productPrice = productDetails.subscriptionOfferDetails?.first()
                                    ?.pricingPhases
                                    ?.pricingPhaseList
                                    ?.first()
                                Subscription(
                                    productId = productDetails.productId,
                                    type = productDetails.productType,
                                    price = productPrice?.priceAmountMicros
                                        ?.toDouble()
                                        ?.div(1000000) ?: 0.0,
                                    priceFormatted = productPrice?.formattedPrice ?: "",
                                    productDetails = productDetails
                                )
                            }
                        }
                        is GoogleBillingResponse.Failure -> {
                            throw IllegalStateException("Error trying to fetch subscriptions: ${response.result}")
                        }
                    }
                    localProductDetails
                }
                .subscribeOn(scheduler)
        } else {
            Single.just(localProductDetails)
        }
    }

    override fun showProductCheckout(
        activity: Activity,
        productDetail: ProductDetails
    ): Completable {
        return api.showProductCheckout(activity, productDetail)
            .flatMapCompletable { response ->
                when (response) {
                    is GoogleBillingResponse.Success -> {
                        Completable.complete()
                    }
                    is GoogleBillingResponse.Failure -> {
                        if (response.result == BillingClient.BillingResponseCode.USER_CANCELED) {
                            throw UserCancelledException
                        } else {
                            throw IllegalStateException("Error trying to purchase: ${response.result}")
                        }
                    }
                }
            }
    }

    object UserCancelledException : Throwable()
}
