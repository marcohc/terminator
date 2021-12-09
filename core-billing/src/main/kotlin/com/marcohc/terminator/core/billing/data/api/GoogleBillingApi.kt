package com.marcohc.terminator.core.billing.data.api

import android.app.Activity
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.android.billingclient.api.*
import com.marcohc.terminator.core.billing.data.entities.ProductEntity
import com.marcohc.terminator.core.billing.data.entities.PurchaseEntity
import com.marcohc.terminator.core.billing.domain.PurchaseEventBus
import com.marcohc.terminator.core.billing.domain.internal.DeleteAllPurchasesUseCase
import com.marcohc.terminator.core.billing.domain.internal.DeleteAndSavePurchasesUseCase
import com.marcohc.terminator.core.billing.domain.internal.SaveProductsUseCase
import com.marcohc.terminator.core.utils.executeCompletable
import com.marcohc.terminator.core.utils.executeCompletableOnIo
import com.marcohc.terminator.core.utils.executeFunctionOnIo
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

internal class GoogleBillingApi(
    private val context: Context,
    private val skuList: List<String>,
    private val saveProductsUseCase: SaveProductsUseCase,
    private val deleteAllPurchasesUseCase: DeleteAllPurchasesUseCase,
    private val deleteAndSavePurchasesUseCase: DeleteAndSavePurchasesUseCase,
    private val purchaseEventBus: PurchaseEventBus
) : BillingApi,
    BillingClientStateListener,
    DefaultLifecycleObserver {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var billingClient: BillingClient

    override fun onCreate(owner: LifecycleOwner) {

        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            // This listener gets trigger when initializing the app or the user buys something
            .setListener { billingResult, purchasesList ->
                val responseCode = billingResult.responseCode
                Timber.v("onPurchasesUpdated: $responseCode")
                logBillingResult(responseCode)
                if (responseCode == BillingClient.BillingResponseCode.OK) {
                    processPurchaseList(purchasesList, true)
                }
            }
            .build()
        if (!billingClient.isReady) {
            Timber.v("BillingClient: Start connection...")
            billingClient.startConnection(this)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        Timber.v("onBillingSetupFinished: $responseCode")
        logBillingResult(responseCode)
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            queryAvailableSubscriptions()
            queryPurchasedSubscriptions()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (billingClient.isReady) {
            Timber.v("BillingClient can only be used once -- closing")
            billingClient.endConnection()
        }
        compositeDisposable.clear()
    }

    override fun onBillingServiceDisconnected() {
        Timber.v("onBillingServiceDisconnected")
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
    }

    override fun launchBillingFlow(activity: Activity, product: ProductEntity) = Completable
        .fromAction {
            if (billingClient.isReady) {
                val purchaseParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(SkuDetails(product.originalJson))
                    .build()

                billingClient.launchBillingFlow(activity, purchaseParams)

                Timber.v("Launching billing flow...")
            } else {
                Timber.e("BillingClient is not ready to start billing flow")
            }
        }

    // It does nothing, it's just a method to expose the logic to clear the subscription for development
    override fun clearAll() = Completable.complete()

    private fun queryAvailableSubscriptions() {
        val params = SkuDetailsParams.newBuilder()
            .apply {
                setSkusList(skuList)
                setType(BillingClient.SkuType.SUBS)
            }
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult: BillingResult, skuDetailsList: List<SkuDetails>? ->

            val responseCode = billingResult.responseCode
            if (responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                Timber.v("SkuDetails query failed with response: $responseCode")
                logBillingResult(responseCode)
                val productsList = skuDetailsList.map { skuDetail ->
                    ProductEntity(
                        skuDetail.sku,
                        skuDetail.type,
                        skuDetail.priceAmountMicros.toDouble().div(1000000),
                        skuDetail.price,
                        // The way to getById the original json
                        skuDetail.toString().substring("SkuDetails: ".length)
                    )
                }
                compositeDisposable.executeCompletableOnIo {
                    saveProductsUseCase.execute(productsList)
                }
            } else {
                Timber.v("SkuDetails query responded with success. List: $skuDetailsList")
                logBillingResult(responseCode)
            }
        }
    }

    private fun queryPurchasedSubscriptions() {
        compositeDisposable.executeFunctionOnIo {
            processPurchaseList(billingClient.queryPurchases(BillingClient.SkuType.SUBS).purchasesList)
        }
    }

    private fun processPurchaseList(purchasesList: List<Purchase>?, notifyUser: Boolean = false) {
        if (purchasesList.isNullOrEmpty()) {
            compositeDisposable.executeCompletableOnIo {
                Timber.v("processPurchaseList: no purchases, clean db")
                deleteAllPurchasesUseCase.execute()
            }
        } else {
            val lastPurchase = purchasesList.maxByOrNull { it.purchaseTime }
            if (lastPurchase != null) {
                // TODO: This must be tested because they added a list of skus instead of only one
                // Store purchase
                compositeDisposable.executeCompletableOnIo {
                    val sku = lastPurchase.skus.firstOrNull()
                    if (sku != null) {
                        deleteAndSavePurchasesUseCase.execute(
                            PurchaseEntity(
                                sku,
                                lastPurchase.originalJson + "|" + lastPurchase.signature
                            )
                        )
                    } else {
                        // No-op
                        Completable.complete()
                    }
                }

                // Notify user
                if (notifyUser) {
                    compositeDisposable.executeCompletable {
                        purchaseEventBus.triggerEvent()
                    }
                }

                // Acknowledge
                if (!lastPurchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(lastPurchase.purchaseToken)
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) { billingResult ->
                        val responseCode = billingResult.responseCode
                        Timber.v("acknowledgePurchase: $responseCode")
                        logBillingResult(responseCode)
                    }
                }
            }
        }
    }

    private fun logBillingResult(responseCode: Int?) {
        when (responseCode) {
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> Timber.v("SERVICE_TIMEOUT")
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> Timber.v("FEATURE_NOT_SUPPORTED")
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> Timber.v("SERVICE_DISCONNECTED")
            BillingClient.BillingResponseCode.OK -> Timber.v("OK")
            BillingClient.BillingResponseCode.USER_CANCELED -> Timber.v("USER_CANCELED")
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> Timber.v("SERVICE_UNAVAILABLE")
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> Timber.v("BILLING_UNAVAILABLE")
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> Timber.v("ITEM_UNAVAILABLE")
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> Timber.v("DEVELOPER_ERROR")
            BillingClient.BillingResponseCode.ERROR -> Timber.v("ERROR")
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> Timber.v("ITEM_ALREADY_OWNED")
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> Timber.v("ITEM_NOT_OWNED")
        }
    }
}
