package com.marcohc.terminator.core.billing.domain

import android.app.Activity
import com.marcohc.terminator.core.billing.data.api.BillingApi
import com.marcohc.terminator.core.billing.data.repositories.ProductRepository
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

class PurchaseSubscriptionUseCase(
    private val billingApi: BillingApi,
    private val productRepository: ProductRepository
) {

    fun execute(activity: Activity, sku: String) = productRepository.getById(sku)
        .observeOn(AndroidSchedulers.mainThread())
        .flatMapCompletable { productOptional ->
            val product = productOptional.toNullable()
            if (product == null) {
                Completable.error(IllegalArgumentException("Unrecognized sku: $sku"))
            } else {
                billingApi.launchBillingFlow(activity, product)
            }
        }
}
