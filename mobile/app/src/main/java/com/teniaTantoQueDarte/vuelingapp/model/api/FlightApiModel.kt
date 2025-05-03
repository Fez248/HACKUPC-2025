package com.teniaTantoQueDarte.vuelingapp.model.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FlightApiModel(
    @SerialName("flightNumber")
    val flightNumber: String,
    @SerialName("origin")
    val origin: String,
    @SerialName("destination")
    val destination: String,
    @SerialName("departureTime")
    val departureTime: String,
    @SerialName("arrivalTime")
    val arrivalTime: String,
    @SerialName("status")
    val status: String,
    @SerialName("door")
    val door: String,
    @SerialName("gate")
    val gate: String,
    @SerialName("terminal")
    val terminal: String
)