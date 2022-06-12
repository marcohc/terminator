package com.marcohc.terminator.core.billing.data.api

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.marcohc.terminator.core.billing.data.entities.PurchaseEntity
import com.marcohc.terminator.core.billing.domain.DeleteAllPurchasesUseCase
import com.marcohc.terminator.core.billing.domain.DeleteAndSavePurchasesUseCase
import com.marcohc.terminator.core.utils.executeCompletableOnIo
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

internal class GoogleBillingApi(
    private val context: Context,
    private val productIdsList: List<String>,
    private val deleteAllPurchasesUseCase: DeleteAllPurchasesUseCase,
    private val deleteAndSavePurchasesUseCase: DeleteAndSavePurchasesUseCase,
) : BillingApi {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var billingClient: BillingClient
    private val purchaseSubject = PublishSubject.create<GoogleBillingResponse<List<Purchase>>>()

    override fun connect() {
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            // This listener gets trigger when initializing the app or the user buys something
            .setListener { billingResult, purchases ->
                val responseCode = billingResult.responseCode
                Timber.v("PurchasesUpdatedListener: $responseCode")
                logResponseCode(responseCode)
                if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    purchaseSubject.onNext(GoogleBillingResponse.Success(purchases.toList()))
                } else {
                    purchaseSubject.onNext(GoogleBillingResponse.Failure(responseCode))
                }
            }
            .build()

        Timber.v("BillingClient: Start connection...")
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val responseCode = billingResult.responseCode
                Timber.v("onBillingSetupFinished: $responseCode")
                logResponseCode(responseCode)
                if (responseCode == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    ) { _: BillingResult, purchases: List<Purchase> ->
                        processPurchases(purchases)
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.v("onBillingServiceDisconnected")
                if (!billingClient.isReady) {
                    billingClient.startConnection(this)
                }
            }
        })
    }

    override fun disconnect() {
        if (billingClient.isReady) {
            Timber.v("BillingClient can only be used once -- closing")
            billingClient.endConnection()
        }
        compositeDisposable.clear()
    }

    override fun getSubscriptions(): Single<GoogleBillingResponse<List<ProductDetails>>> {
        return Single.create { emitter ->
            if (billingClient.isReady) {
                billingClient.queryProductDetailsAsync(
                    QueryProductDetailsParams.newBuilder()
                        .setProductList(
                            productIdsList.map { productId ->
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(productId)
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build()
                            }
                        )
                        .build()
                ) { billingResult: BillingResult, productDetailsList: List<ProductDetails> ->
                    val responseCode = billingResult.responseCode
                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                        emitter.onSuccess(GoogleBillingResponse.Success(productDetailsList))
                    } else {
                        emitter.onSuccess(GoogleBillingResponse.Failure(responseCode))
                    }
                }
            } else {
                Timber.e("BillingClient is not ready when trying to fetch subscriptions")
                emitter.onSuccess(GoogleBillingResponse.Failure(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED))
            }
        }
    }

    override fun showProductCheckout(activity: Activity, productDetails: ProductDetails) =
        Completable
            .fromAction {
                if (billingClient.isReady) {
                    billingClient.launchBillingFlow(
                        activity,
                        BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(
                                listOf(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .setOfferToken(productDetails.subscriptionOfferDetails!!.first().offerToken)
                                        .build()
                                )
                            )
                            .build()
                    )

                    Timber.v("Launching billing flow...")
                } else {
                    Timber.e("BillingClient is not ready to start billing flow")
                }
            }
            .andThen(
                purchaseSubject
                    .take(1)
                    .singleOrError()
            )

    private fun processPurchases(purchasesList: List<Purchase>?) {
        if (purchasesList.isNullOrEmpty()) {
            compositeDisposable.executeCompletableOnIo {
                Timber.v("processPurchaseList: no purchases, clean db")
                deleteAllPurchasesUseCase.execute()
            }
        } else {
            val lastPurchase = purchasesList.maxByOrNull { it.purchaseTime }
            if (lastPurchase != null) {
                // Store purchase
                compositeDisposable.executeCompletableOnIo {
                    val productId = lastPurchase.products.firstOrNull()
                    if (productId != null) {
                        deleteAndSavePurchasesUseCase.execute(
                            PurchaseEntity(
                                productId = productId,
                                jsonPlusSignature = lastPurchase.originalJson + "|" + lastPurchase.signature
                            )
                        )
                    } else {
                        // No-op
                        Completable.complete()
                    }
                }

                // Acknowledge
                if (!lastPurchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(lastPurchase.purchaseToken)
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) { billingResult ->
                        val responseCode = billingResult.responseCode
                        Timber.v("acknowledgePurchase: $responseCode")
                        logResponseCode(responseCode)
                    }
                }
            }
        }
    }

    private fun logResponseCode(responseCode: Int?) {
        Timber.v(
            when (responseCode) {
                BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> "SERVICE_TIMEOUT"
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
                BillingClient.BillingResponseCode.OK -> "OK"
                BillingClient.BillingResponseCode.USER_CANCELED -> "USER_CANCELED"
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
                BillingClient.BillingResponseCode.ERROR -> "ERROR"
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
                else -> {
                    ""
                }
            }
        )
    }
}
