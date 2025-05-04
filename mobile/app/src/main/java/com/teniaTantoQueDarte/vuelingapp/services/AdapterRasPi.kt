package com.teniaTantoQueDarte.vuelingapp.services

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.work.ListenableWorker.Result
import androidx.work.workDataOf
import com.google.gson.Gson
import com.teniaTantoQueDarte.vuelingapp.data.db.AppDatabase
import com.teniaTantoQueDarte.vuelingapp.data.repository.FlightRepository
import com.teniaTantoQueDarte.vuelingapp.data.repository.NewRepository
import com.teniaTantoQueDarte.vuelingapp.model.FlightModel
import com.teniaTantoQueDarte.vuelingapp.model.NewModel
import com.teniaTantoQueDarte.vuelingapp.utils.RetrofitClient
import com.teniaTantoQueDarte.vuelingapp.workers.NetworkSyncWorker.Companion.KEY_FLIGHTS_JSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.concurrent.TimeUnit

class AdapterRasPi(private val context: Context) {
    // Inicialización lazy para recursos pesados
    private val gson by lazy { Gson() }

    // Cache para minimizar acceso a disco/red
    private var cachedFlights: List<FlightModel>? = null
    private var lastFetchTime: Long = 0
    private val CACHE_VALIDITY_PERIOD = TimeUnit.MINUTES.toMillis(15) // 15 minutos

    // Acceso optimizado a la base de datos

    private val flightRepository by lazy { FlightRepository(context) }

    // Variables para caché de noticias
    private var cachedNews: List<NewModel>? = null
    private var lastNewsFetchTime: Long = 0
    private val newsRepository by lazy { NewRepository(context) }

    companion object {
        const val KEY_NEWS_JSON = "key_news_json"
    }



    // Clave pública para verificar datos
    private val publicKeyBase64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxTXj2e0YQMcttm/zGb7l" +
            "g2FjxSWZLoH0oBGUzROzl3pf3MaTo+jznszz7FK/y3gSyUlkc3drQ60MoHyKd3jd" +
            "7DdYujsYmvHWF5IYxjyTa5r+W0b3FTXorKROyR7cp7qM98z5ANq+whmMfQduQgGP" +
            "ZDE0HrURv42MSckilD7KWH3G7b0nXOFVMSVfiPt9sjf4gnV5LLDoMHz/Dl3AtSPE" +
            "CaEO3tKu/lpH6ZBUjp8htKnsEY+bqWGzL3A9qyCVqvu63m3pyTy9ywMpawwT0GCZ" +
            "JBradUbwGC60hex1aeMyx56aHeKact7bU5WXMBju7EPOnq3zkg0yJJQXMnnrPzxB" +
            "NwIDAQAB" // Reemplazar con la clave real

    suspend fun getFlights(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Verificar si hay datos en caché válidos
                val currentTime = System.currentTimeMillis()
                if (cachedFlights != null && (currentTime - lastFetchTime < CACHE_VALIDITY_PERIOD)) {
                    return@withContext createSuccessResult(cachedFlights!!)
                }

                // 2. Intentar obtener datos de la red
                try {
                    val apiResponse = RetrofitClient.apiService.getFlights()
                    val signatureBase64 = apiResponse.firstOrNull()?.signature ?: ""

                    // Verificar autenticidad de los datos
                    val isValid = verifyData(apiResponse, signatureBase64, publicKeyBase64)
                    if (!isValid) {
                        Log.w("AdapterRasPi", "Verificación de firma fallida, usando datos locales")
                        return@withContext fallbackToLocal()
                    }

                    // Procesar datos validados
                    val flights = ArrayList<FlightModel>(apiResponse.size).apply {
                        apiResponse.mapTo(this) { apiModel ->
                            FlightModel(
                                ArriveTime = apiModel.arrivalTime,
                                DepartTime = apiModel.departureTime,
                                FromShort = apiModel.origin,
                                ToShort = apiModel.destination,
                                Status = apiModel.status,
                                FlightNumber = apiModel.flightNumber,
                                updateTime = currentTime.toString(),
                                favorito = false
                            )
                        }
                    }

                    // Actualizar caché y BD
                    cachedFlights = flights
                    lastFetchTime = currentTime
                    updateLocalDatabase(flights)

                    return@withContext createSuccessResult(flights)
                } catch (e: Exception) {
                    Log.w("AdapterRasPi", "Error de red, usando datos locales", e)
                    return@withContext fallbackToLocal()
                }
            } catch (e: Exception) {
                Log.e("AdapterRasPi", "Error crítico", e)
                return@withContext Result.failure()
            }
        }
    }

    // Verificación de autenticidad usando firma digital
    private fun verifyData(data: Any, signatureBase64: String, publicKeyBase64: String): Boolean {
        return try {
            // Convertir clave pública desde Base64
            val keyBytes = Base64.decode(publicKeyBase64, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey: PublicKey = keyFactory.generatePublic(keySpec)

            // Convertir datos a JSON string y luego a bytes
            val dataJson = gson.toJson(data)
            val dataBytes = dataJson.toByteArray(Charsets.UTF_8)

            // Inicializar verificación de firma
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initVerify(publicKey)
            signature.update(dataBytes)

            // Decodificar firma y verificar
            val signatureBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
            signature.verify(signatureBytes)
        } catch (e: Exception) {
            Log.e("AdapterRasPi", "Error verificando firma", e)
            false
        }
    }

    // Recuperación de datos locales
    private suspend fun fallbackToLocal(): Result {
        val localFlights = flightRepository.getAllFlights()
        return if (localFlights.isNotEmpty()) {
            cachedFlights = localFlights
            createSuccessResult(localFlights)
        } else {
            Result.failure()
        }
    }

    // Crear resultado exitoso
    private fun createSuccessResult(flights: List<FlightModel>): Result {
        val json = gson.toJson(flights)
        return Result.success(workDataOf(KEY_FLIGHTS_JSON to json))
    }

    // Actualizar base de datos local
    private suspend fun updateLocalDatabase(flights: List<FlightModel>) {
        try {
            flightRepository.insertFlights(flights)
        } catch (e: Exception) {
            Log.e("AdapterRasPi", "Error actualizando BD local", e)
        }
    }

    suspend fun getNews(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Verificar si hay datos en caché válidos
                val currentTime = System.currentTimeMillis()
                if (cachedNews != null && (currentTime - lastNewsFetchTime < CACHE_VALIDITY_PERIOD)) {
                    return@withContext createNewsSuccessResult(cachedNews!!)
                }

                // 2. Intentar obtener datos de la red
                try {
                    val apiResponse = RetrofitClient.apiService.getNews()

                    // Extraer la firma del último elemento y quitarlo de la lista
                    val signatureBase64 = apiResponse.lastOrNull()?.signature ?: ""
                    val newsResponse = if (apiResponse.isNotEmpty()) {
                        apiResponse.dropLast(1)
                    } else {
                        emptyList()
                    }

                    // Verificar autenticidad de los datos
                    val isValid = verifyData(newsResponse, signatureBase64, publicKeyBase64)
                    if (!isValid) {
                        Log.w("AdapterRasPi", "Verificación de firma fallida, usando datos locales")
                        return@withContext fallbackToLocal()
                    }

                    val news = newsResponse.map { apiModel ->
                        NewModel(
                            id = apiModel.id,
                            FlightNumber = apiModel.FlightNumber,
                            Title = apiModel.Title,
                            Content = apiModel.Content,
                            Date = apiModel.Date
                        )
                    }
                    // Actualizar caché y BD
                    cachedNews = news
                    lastNewsFetchTime = currentTime
                    updateLocalNewsDatabase(news)

                    return@withContext createNewsSuccessResult(news)
                } catch (e: Exception) {
                    Log.w("AdapterRasPi", "Error de red obteniendo noticias, usando datos locales", e)
                    return@withContext fallbackToLocalNews()
                }
            } catch (e: Exception) {
                Log.e("AdapterRasPi", "Error crítico obteniendo noticias", e)
                return@withContext Result.failure()
            }
        }
    }

    // Recuperación de datos locales de noticias
    private suspend fun fallbackToLocalNews(): Result {
        val localNews = newsRepository.getAllNews()
        return if (localNews.isNotEmpty()) {
            cachedNews = localNews
            createNewsSuccessResult(localNews)
        } else {
            Result.failure()
        }
    }

    // Crear resultado exitoso para noticias
    private fun createNewsSuccessResult(news: List<NewModel>): Result {
        val json = gson.toJson(news)
        return Result.success(workDataOf(KEY_NEWS_JSON to json))
    }

    // Actualizar base de datos local de noticias
    private suspend fun updateLocalNewsDatabase(news: List<NewModel>) {
        try {
            newsRepository.insertNews(news)
        } catch (e: Exception) {
            Log.e("AdapterRasPi", "Error actualizando noticias en BD local", e)
        }
    }
}