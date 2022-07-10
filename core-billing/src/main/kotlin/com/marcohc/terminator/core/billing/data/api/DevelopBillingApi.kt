package com.marcohc.terminator.core.billing.data.api

import android.app.Activity
import com.android.billingclient.api.Purchase
import com.marcohc.terminator.core.billing.data.entities.PurchaseEntity
import com.marcohc.terminator.core.billing.data.models.Subscription
import com.marcohc.terminator.core.billing.domain.DeleteAllPurchasesUseCase
import com.marcohc.terminator.core.billing.domain.DeleteAndSavePurchasesUseCase
import io.reactivex.Single

internal class DevelopBillingApi(
    private val deleteAllPurchasesUseCase: DeleteAllPurchasesUseCase,
    private val deleteAndSavePurchasesUseCase: DeleteAndSavePurchasesUseCase,
) : BillingApi {

    override fun connect() {
    }

    override fun disconnect() {
        deleteAllPurchasesUseCase.execute().blockingAwait()
    }

    override fun showProductCheckout(
        activity: Activity,
        productId: String
    ): Single<GoogleBillingResponse<List<Purchase>>> {
        return deleteAndSavePurchasesUseCase
            .execute(
                PurchaseEntity(
                    productId = productId,
                    jsonPlusSignature = ""
                )
            )
            .andThen(Single.just(GoogleBillingResponse.Success(emptyList())))
    }

    override fun getSubscriptions(): Single<GoogleBillingResponse<List<Subscription>>> {
        return Single.just(GoogleBillingResponse.Success(emptyList()))
    }
}
