package com.teniaTantoQueDarte.vuelingapp.model

data class FlightModel(
    val ArriveTime: String,
    val DepartTime: String,
    val Seat: String,
    val From: String,
    val To: String,
    val FromShort: String,
    val ToShort: String,
    val Status: String,
    val Reason: String,
    val FlightNumber: String
)
