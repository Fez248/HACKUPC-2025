package com.teniaTantoQueDarte.vuelingapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.teniaTantoQueDarte.vuelingapp.data.model.FlightModel

@Dao
interface FlightDao {
    @Query("SELECT * FROM flights ORDER BY updateTime DESC")
    suspend fun getAllFlights(): List<FlightModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flights: List<FlightModel>)

    @Query("DELETE FROM flights")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM flights")
    suspend fun getCount(): Int
}