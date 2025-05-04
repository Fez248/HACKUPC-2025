package com.teniaTantoQueDarte.vuelingapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.teniaTantoQueDarte.vuelingapp.data.model.NewModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NewDao {
    @Query("SELECT * FROM news")
    suspend fun getAllNews(): List<NewModel>

    @Query("SELECT * FROM news")
    fun observeAllNews(): Flow<List<NewModel>>

    @Query("SELECT * FROM news WHERE FlightNumber = :flightNumber")
    suspend fun getNewsByFlightNumber(flightNumber: String): List<NewModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(news: List<NewModel>)

    @Query("DELETE FROM news")
    suspend fun deleteAll()
}