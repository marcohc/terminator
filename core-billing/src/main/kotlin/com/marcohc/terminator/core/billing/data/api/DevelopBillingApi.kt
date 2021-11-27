package com.marcohc.terminator.core.billing.data.api

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.android.billingclient.api.BillingClient
import com.marcohc.terminator.core.billing.data.entities.ProductEntity
import com.marcohc.terminator.core.billing.data.entities.PurchaseEntity
import com.marcohc.terminator.core.billing.domain.PurchaseEventBus
import com.marcohc.terminator.core.billing.domain.internal.DeleteAllPurchasesUseCase
import com.marcohc.terminator.core.billing.domain.internal.DeleteAndSavePurchasesUseCase
import com.marcohc.terminator.core.billing.domain.internal.SaveProductsUseCase
import com.marcohc.terminator.core.utils.executeCompletableOnIo
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable

// TODO: Move this development code to debug buildType so it's not dragged into production
internal class DevelopBillingApi(
    private val skuList: List<String>,
    private val saveProductsUseCase: SaveProductsUseCase,
    private val deleteAllPurchasesUseCase: DeleteAllPurchasesUseCase,
    private val deleteAndSavePurchasesUseCase: DeleteAndSavePurchasesUseCase,
    private val purchaseEventBus: PurchaseEventBus
) : BillingApi {

    private val compositeDisposable = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        compositeDisposable.executeCompletableOnIo {
            saveProductsUseCase.execute(
                skuList.map { sku ->
                    ProductEntity(
                        sku = sku,
                        type = BillingClient.SkuType.SUBS,
                        price = 1.99,
                        priceFormatted = "1,99€",
                        originalJson = ""
                    )
                }
            )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        compositeDisposable.clear()
    }

    override fun launchBillingFlow(activity: Activity, product: ProductEntity): Completable {
        return deleteAndSavePurchasesUseCase.execute(PurchaseEntity(product.sku, ""))
            .andThen(purchaseEventBus.triggerEvent())
    }

    override fun clearAll() = deleteAllPurchasesUseCase.execute()
}
