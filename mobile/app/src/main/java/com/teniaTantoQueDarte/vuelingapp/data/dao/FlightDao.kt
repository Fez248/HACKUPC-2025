package com.teniaTantoQueDarte.vuelingapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.teniaTantoQueDarte.vuelingapp.data.model.FlightModel
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {
    @Query("SELECT * FROM flights")
    suspend fun getAllFlights(): List<FlightModel>

    @Query("SELECT * FROM flights WHERE favorito = 1")
    suspend fun getFavoriteFlights(): List<FlightModel>

    @Query("SELECT * FROM flights WHERE favorito = 1")
    fun observeFavoriteFlights(): Flow<List<FlightModel>>

    @Query("UPDATE flights SET favorito = :isFavorite WHERE FlightNumber = :flightNumber")
    suspend fun updateFavoriteStatus(flightNumber: String, isFavorite: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flights: List<FlightModel>)
}