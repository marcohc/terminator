package com.marcohc.terminator.core.billing.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "p")
class ProductEntity(
    @ColumnInfo(name = "a")
    @PrimaryKey
    val sku: String,
    @ColumnInfo(name = "b")
    val type: String,
    @ColumnInfo(name = "c")
    val price: Double,
    @ColumnInfo(name = "d")
    val priceFormatted: String,
    @ColumnInfo(name = "e")
    val originalJson: String
)
