package com.teniaTantoQueDarte.vuelingapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "main_user",
    val points: Int = 0,
    val isSharingMode: Boolean = false
)