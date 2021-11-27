package com.marcohc.terminator.core.billing.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.android.billingclient.api.BillingClient
import com.marcohc.terminator.core.billing.data.entities.ProductEntity
import io.reactivex.Completable
import io.reactivex.Single

@Dao
internal interface ProductDao {

    @Query("SELECT * FROM p WHERE a = :id")
    fun getById(id: String): Single<ProductEntity>

    @Query("SELECT * FROM p WHERE b = '${BillingClient.SkuType.SUBS}'")
    fun getSubscriptions(): Single<List<ProductEntity>>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(products: List<ProductEntity>): Completable
}
