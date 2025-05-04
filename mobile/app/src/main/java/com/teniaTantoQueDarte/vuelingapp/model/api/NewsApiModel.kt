package com.teniaTantoQueDarte.vuelingapp.model.api

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class NewsApiModel(
    val id: String?,
    @SerializedName("flightNumber") val FlightNumber: String?,
    @SerializedName("title") val Title: String?,
    @SerializedName("content") val Content: String?,
    @SerializedName("date") val Date: String?,
    val signature: String?
)