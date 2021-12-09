package com.marcohc.terminator.core.billing.data.api

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import com.marcohc.terminator.core.billing.data.entities.ProductEntity
import io.reactivex.Completable

interface BillingApi : DefaultLifecycleObserver {
    fun launchBillingFlow(activity: Activity, product: ProductEntity): Completable
    fun clearAll(): Completable
}
