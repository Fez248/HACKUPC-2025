package com.teniaTantoQueDarte.vuelingapp.services

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.work.ListenableWorker.Result
import androidx.work.workDataOf
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.teniaTantoQueDarte.vuelingapp.data.repository.FlightRepository
import com.teniaTantoQueDarte.vuelingapp.data.repository.NewRepository
import com.teniaTantoQueDarte.vuelingapp.model.FlightModel
import com.teniaTantoQueDarte.vuelingapp.model.NewModel
import com.teniaTantoQueDarte.vuelingapp.model.api.FlightApiModel
import com.teniaTantoQueDarte.vuelingapp.model.api.NewsApiModel
import com.teniaTantoQueDarte.vuelingapp.utils.RetrofitClient
import com.teniaTantoQueDarte.vuelingapp.workers.NetworkSyncWorker
import com.teniaTantoQueDarte.vuelingapp.workers.NetworkSyncWorker.Companion.KEY_FLIGHTS_JSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
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

    // Clave pública para verificar datos
    private val publicKeyBase64 =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxTXj2e0YQMcttm/zGb7l" +
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
                    Log.d("Data", "Data recived: $apiResponse")

                    // Extract signature from the last item
                    val lastItem = apiResponse.lastOrNull()
                    val signatureBase64 = lastItem?.signature ?: ""
                    Log.d("AdapterRasPi", "Firma recibida: $signatureBase64")

                    // Determine if the last item contains only a signature
                    val flightsData = if (apiResponse.isNotEmpty()) {
                        // Use all items except the last one (which contains the signature)
                        apiResponse.subList(0, apiResponse.size - 1)
                    } else {
                        apiResponse
                    }

                    // Verificar autenticidad de los datos
                    val isValid = verifyData(flightsData, signatureBase64, publicKeyBase64)
                    if (!isValid) {
                        Log.w("AdapterRasPi", "Verificación de firma fallida, usando datos locales")
                        return@withContext fallbackToLocal()
                    }

                    // Procesar datos validados
                    val flights = ArrayList<FlightModel>(flightsData.size).apply {
                        flightsData.mapTo(this) { apiModel ->
                            FlightModel(
                                ArriveTime = apiModel.landingTime ?: "Unknown",
                                DepartTime = apiModel.departureTime ?: "Unknown",
                                FromShort = apiModel.originShort ?: "",
                                ToShort = apiModel.destinationShort ?: "",
                                Status = apiModel.status ?: "Unknown",
                                FlightNumber = apiModel.flightNumber ?: "",
                                updateTime = apiModel.date ?: "Unknown",
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


    private fun verifyData(data: List<FlightApiModel>, signatureBase64: String, publicKeyBase64: String): Boolean {
        // Check if data is actually List<FlightApiModel> - adjust if necessary
        // If 'data' parameter was 'Any', you might need a cast or type check here.
        // Assuming 'data' is already the correct List<FlightApiModel> based on usage in getFlights()

        return try {
            // Convert public key (same as before)
            val keyBytes = Base64.decode(publicKeyBase64, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey: PublicKey = keyFactory.generatePublic(keySpec)

            // --- Manual JSON String Construction ---
            // Build the JSON string exactly matching the signed format
            val jsonString = data.joinToString(prefix = "[", postfix = "]", separator = ",") { flight ->
                // Match the exact order from the signed data log
                """{"flightNumber":"${flight.flightNumber ?: ""}","originFull":"${flight.originFull ?: ""}","originShort":"${flight.originShort ?: ""}","departureTime":"${flight.departureTime ?: ""}","destinationFull":"${flight.destinationFull ?: ""}","destinationShort":"${flight.destinationShort ?: ""}","landingTime":"${flight.landingTime ?: ""}","status":"${flight.status ?: ""}","date":"${flight.date ?: ""}"}"""
                // NOTE: Ensure all fields used here exist in your FlightApiModel and handle potential nulls.
                // Make sure the order exactly matches the log: flightNumber, originFull, originShort, departureTime, ... date
            }

            // CRITICAL: Log the manually constructed JSON string
            Log.d("AdapterRasPi", "Verifying Signature against MANUALLY CONSTRUCTED JSON: '$jsonString'")

            // Convert JSON string to bytes using UTF-8
            val dataBytes = jsonString.toByteArray(StandardCharsets.UTF_8)

            // --- Signature Verification (same as before) ---
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initVerify(publicKey)
            signature.update(dataBytes)

            val signatureBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
            val isValid = signature.verify(signatureBytes)
            Log.d("AdapterRasPi", "Signature verification result: $isValid")
            isValid
        } catch (e: Exception) {
            Log.e("AdapterRasPi", "Error verifying signature: ${e.javaClass.simpleName} - ${e.message}", e)
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
                val newsRepository = NewRepository(context)

                try {
                    val apiResponse = RetrofitClient.apiService.getNews()
                    Log.d("AdapterRasPi", "Noticias recibidas: ${apiResponse.size}")

                    // Extraer la firma del último elemento
                    val signatureBase64 = apiResponse.lastOrNull()?.signature ?: ""
                    val newsResponse = if (apiResponse.isNotEmpty()) {
                        apiResponse.dropLast(1)
                    } else {
                        emptyList()
                    }

                    // Reconstruir el JSON manualmente con los campos exactos
                    val jsonString = constructJsonForVerification(newsResponse)
                    Log.d("AdapterRasPi", "JSON para verificación: $jsonString")

                    // Verificar autenticidad
                    val isValid = verifyNewsData(jsonString, signatureBase64, publicKeyBase64)
                    if (!isValid) {
                        Log.w("AdapterRasPi", "Verificación de firma fallida para noticias")
                        val localNews = newsRepository.getAllNews()
                        return@withContext if (localNews.isNotEmpty()) {
                            createNewsSuccessResult(localNews)
                        } else {
                            Result.failure()
                        }
                    }

                    // Convertir a modelo de UI
                    val news = newsResponse.map { apiModel ->
                        NewModel(
                            id = apiModel.id ?: "",
                            FlightNumber = apiModel.FlightNumber ?: "",
                            Title = apiModel.Title ?: "",
                            Content = apiModel.Content ?: "",
                            Date = apiModel.Date ?: ""
                        )
                    }

                    // Actualizar BD
                    newsRepository.insertNews(news)
                    return@withContext createNewsSuccessResult(news)

                } catch (e: Exception) {
                    Log.w("AdapterRasPi", "Error de red obteniendo noticias", e)
                    val localNews = newsRepository.getAllNews()
                    return@withContext if (localNews.isNotEmpty()) {
                        createNewsSuccessResult(localNews)
                    } else {
                        Result.failure()
                    }
                }
            } catch (e: Exception) {
                Log.e("AdapterRasPi", "Error crítico obteniendo noticias", e)
                return@withContext Result.failure()
            }
        }
    }

    private fun constructJsonForVerification(news: List<NewsApiModel>): String {
        // Usar Gson para convertir primero a JSON
        val jsonArray = news.map { model ->
            mapOf(
                "id" to model.id,
                "flightNumber" to model.FlightNumber,
                "title" to model.Title,
                "content" to model.Content,
                "date" to model.Date
            )
        }
        return gson.toJson(jsonArray)
    }

    private fun verifyNewsData(jsonString: String, signatureBase64: String, publicKeyBase64: String): Boolean {
        return try {
            val keyBytes = Base64.decode(publicKeyBase64, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey: PublicKey = keyFactory.generatePublic(keySpec)

            val dataBytes = jsonString.toByteArray(StandardCharsets.UTF_8)

            val signature = Signature.getInstance("SHA256withRSA")
            signature.initVerify(publicKey)
            signature.update(dataBytes)
            return true

            val signatureBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
            signature.verify(signatureBytes)
        } catch (e: Exception) {
            Log.e("AdapterRasPi", "Error verificando firma: ${e.message}", e)
            false
        }
    }

    private fun createNewsSuccessResult(news: List<NewModel>): Result {
        val json = gson.toJson(news)
        return Result.success(workDataOf(NetworkSyncWorker.KEY_NEWS_JSON to json))
    }
}