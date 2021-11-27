package com.marcohc.terminator.core.billing.data.dao

import androidx.room.*
import com.marcohc.terminator.core.billing.data.entities.PurchaseEntity
import io.reactivex.Flowable

@Dao
internal interface PurchaseDao {

    @Query("SELECT * FROM c limit 1")
    fun observeLimit1(): Flowable<PurchaseEntity>

    @Query("SELECT COUNT(*) FROM c")
    fun observeCount(): Flowable<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(purchase: PurchaseEntity)

    @Query("DELETE FROM c")
    fun deleteAll()

    @Transaction
    fun deleteAllAndSave(purchase: PurchaseEntity) {
        deleteAll()
        save(purchase)
    }
}
