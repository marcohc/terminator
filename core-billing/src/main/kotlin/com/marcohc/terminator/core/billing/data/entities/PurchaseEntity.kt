package com.marcohc.terminator.core.billing.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "c")
class PurchaseEntity(
    @ColumnInfo(name = "y")
    @PrimaryKey
    val sku: String,
    @ColumnInfo(name = "z")
    val jsonPlusSignature: String
)
