package com.teniaTantoQueDarte.vuelingapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news")
data class NewModel(
    @PrimaryKey
    val id: String,
    val FlightNumber: String,
    val Title: String,
    val Content: String,
    val Date: String
)

