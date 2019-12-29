package com.marcohc.terminator.sample.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marcohc.terminator.sample.data.model.Venue

@Dao
internal abstract class VenueDao {

    @Query("SELECT * FROM venues WHERE id = (:id) LIMIT 1")
    abstract fun getById(id: String): Venue?

    @Query("SELECT * FROM venues WHERE city = (:city)")
    abstract fun getByCity(city: String): List<Venue>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(venue: Venue): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveAll(venues: Array<Venue>): List<Long>

}
