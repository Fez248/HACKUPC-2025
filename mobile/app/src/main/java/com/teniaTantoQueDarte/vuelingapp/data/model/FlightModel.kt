package com.teniaTantoQueDarte.vuelingapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flights")
data class FlightModel(
    @PrimaryKey
    val FlightNumber: String,
    val ArriveTime: String,
    val DepartTime: String,
    val FromShort: String,
    val ToShort: String,
    val Status: String,
    val updateTime: String,
    val favorito: Boolean
)