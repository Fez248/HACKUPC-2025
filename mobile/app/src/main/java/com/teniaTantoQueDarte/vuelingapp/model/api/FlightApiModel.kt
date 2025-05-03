package com.teniaTantoQueDarte.vuelingapp.model.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FlightApiModel(
    @SerialName("flightNumber")
    val flightNumber: String,
    @SerialName("originFull")
    val originFull: String,
    @SerialName("originShort")
    val originShort: String,
    @SerialName("destinationFull")
    val destinationFull: String,
    @SerialName("destinationShort")
    val destinationShort: String,
    @SerialName("departureTime")
    val departureTime: String,
    @SerialName("landingTime")
    val landingTime: String,
    @SerialName("status")
    val status: String,
    @SerialName("date")
    val date: String,
    @SerialName("signature")
    val signature: String
)