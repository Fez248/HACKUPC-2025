package com.teniaTantoQueDarte.vuelingapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.teniaTantoQueDarte.vuelingapp.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1")
    fun getUserById(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET isSharingMode = :isSharing WHERE id = 1")
    suspend fun updateSharingMode(isSharing: Boolean)

    @Query("UPDATE users SET points = points + :points WHERE id = 1")
    suspend fun addPoints(points: Int)

    // Método optimizado para limpieza - solo actualiza en lugar de borrar
    @Query("UPDATE users SET lastSync = :newTimestamp WHERE lastSync < :threshold AND lastSync IS NOT NULL")
    suspend fun updateOldRecords(threshold: Long, newTimestamp: Long): Int

}