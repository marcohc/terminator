package com.marcohc.terminator.core.billing.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.marcohc.terminator.core.billing.data.dao.PurchaseDao
import com.marcohc.terminator.core.billing.data.entities.ProductEntity
import com.marcohc.terminator.core.billing.data.entities.PurchaseEntity

@Database(
    entities = [
        ProductEntity::class,
        PurchaseEntity::class
    ],
    version = 1,
    exportSchema = false
)
internal abstract class BillingDatabase : RoomDatabase() {

    abstract fun purchaseDao(): PurchaseDao

    companion object {
        @Volatile
        private var INSTANCE: BillingDatabase? = null

        private const val DATABASE_NAME = "system_cache_v2.db"

        fun getInstance(context: Context): BillingDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(
                        context.applicationContext
                    ).also {
                        INSTANCE = it
                    }
            }

        /**
         * Set up the database configuration.
         * The SQLite database is only created when it's accessed for the first time.
         */
        private fun buildDatabase(appContext: Context): BillingDatabase {
            return Room
                .databaseBuilder(
                    appContext,
                    BillingDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
