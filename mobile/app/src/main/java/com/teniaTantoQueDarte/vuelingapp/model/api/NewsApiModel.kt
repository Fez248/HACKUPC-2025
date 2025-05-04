package com.teniaTantoQueDarte.vuelingapp.model.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsApiModel(
    @SerialName("id")
    val id: String,
    @SerialName("flightNumber")
    val FlightNumber: String,
    @SerialName("title")
    val Title: String,
    @SerialName("content")
    val Content: String,
    @SerialName("date")
    val Date: String,
    @SerialName("signature")
    val signature: String
)