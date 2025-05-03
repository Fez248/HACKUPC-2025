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
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String = "main_user"): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET isSharingMode = :isSharing WHERE id = :userId")
    suspend fun updateSharingMode(userId: String = "main_user", isSharing: Boolean)

    @Query("UPDATE users SET points = :points WHERE id = :userId")
    suspend fun updatePoints(userId: String = "main_user", points: Int)
}