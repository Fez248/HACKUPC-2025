package com.teniaTantoQueDarte.vuelingapp.model

data class FlightModel(
    val ArriveTime: String,
    val DepartTime: String,
    val FromShort: String,
    val ToShort: String,
    val Status: String,
    val FlightNumber: String,
    val updateTime: String,
    val favorito: Boolean = false
)
