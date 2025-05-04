package com.teniaTantoQueDarte.vuelingapp.services

import com.teniaTantoQueDarte.vuelingapp.data.model.NewModel
import com.teniaTantoQueDarte.vuelingapp.model.api.FlightApiModel
import com.teniaTantoQueDarte.vuelingapp.model.api.NewsApiModel
import retrofit2.http.GET

interface FlightApiService {
    @GET("api/ALL/data")
    suspend fun getFlights(): List<FlightApiModel>

    @GET("api/ALL/news")
    suspend fun getNews(): List<NewsApiModel>
}