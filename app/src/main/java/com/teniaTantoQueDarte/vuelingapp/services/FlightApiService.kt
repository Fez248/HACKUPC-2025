package com.teniaTantoQueDarte.vuelingapp.services

import com.teniaTantoQueDarte.vuelingapp.model.api.FlightApiModel
import retrofit2.http.GET

interface FlightApiService {
    @GET("api/ALL/data")
    suspend fun getFlights(): List<FlightApiModel>
}