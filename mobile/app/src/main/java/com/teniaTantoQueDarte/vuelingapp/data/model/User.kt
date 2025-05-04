package com.teniaTantoQueDarte.vuelingapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 1, // Usuario único por app
    val points: Int = 0,
    val isSharingMode: Boolean = false,
    val moreBatteryGuy: Boolean = false,
    val lastSync: Long? = null // Campo para tracking de última sincronización
)