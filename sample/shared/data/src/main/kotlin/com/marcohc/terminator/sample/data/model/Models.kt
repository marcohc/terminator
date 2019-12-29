package com.marcohc.terminator.sample.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venues")
data class Venue(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: String = "",
        @ColumnInfo(name = "name")
        var name: String = "",
        @ColumnInfo(name = "description")
        var description: String = "",
        @ColumnInfo(name = "city")
        var city: String = "",
        @ColumnInfo(name = "location")
        var location: String = "",
        @ColumnInfo(name = "rating")
        var rating: Double = 0.0,
        @ColumnInfo(name = "phone")
        var phone: String = "",
        @ColumnInfo(name = "pictureUrl")
        var pictureUrl: String = ""
)
