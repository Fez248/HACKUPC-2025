package com.teniaTantoQueDarte.vuelingapp.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teniaTantoQueDarte.vuelingapp.ui.screen.CompletedPhrase
import com.teniaTantoQueDarte.vuelingapp.utils.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


// Modelos de datos para API
data class PhraseResponse(
    val phrase: String,
    val round: Int
)

data class AddPhraseRequest(
    val phrase: String,
    val round: Int
)

data class ApiResponse(
    val message: String,
    val phrase: PhraseResponse
)

// Interfaz de API
interface GameApiService {
    @GET("api/game")
    suspend fun getRandomPhrase(): Response<PhraseResponse>

    @GET("api/game/ended")
    suspend fun getEndedGames(): Response<List<PhraseResponse>>

    @POST("api/game/add")
    suspend fun addPhrase(@Body request: AddPhraseRequest): Response<ApiResponse>
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    // Estado UI
    private val _currentPhrase = MutableStateFlow<String>("Loading...")
    val currentPhrase: StateFlow<String> = _currentPhrase.asStateFlow()

    private val _currentRound = MutableStateFlow(0)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

    private val _totalRounds = MutableStateFlow(10)
    val totalRounds: StateFlow<Int> = _totalRounds.asStateFlow()

    private val _completedPhrases = MutableStateFlow<List<CompletedPhrase>>(emptyList())
    val completedPhrases: StateFlow<List<CompletedPhrase>> = _completedPhrases.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Almacenamos la frase actual para poder enviarla al siguiente endpoint
    private var currentPhraseData: PhraseResponse? = null

    // Servicio API
    private val gameService = RetrofitClient.retrofit.create(GameApiService::class.java)

    init {
        loadRandomPhrase()
        loadEndedGames()
    }

    private fun loadRandomPhrase() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val response = gameService.getRandomPhrase()
                if (response.isSuccessful && response.body() != null) {
                    val phraseData = response.body()!!
                    currentPhraseData = phraseData
                    _currentPhrase.value = phraseData.phrase
                    _currentRound.value = phraseData.round
                } else {
                    _error.value = "Couldn't load: ${response.message()}"
                    // Usar frase de ejemplo para desarrollo
                    _currentPhrase.value = "Esta es una frase de ejemplo para el juego"
                    _currentRound.value = 1
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error al cargar frase", e)
                _error.value = "Connection error: ${e.message}"
                // Usar frase de ejemplo para desarrollo
                _currentPhrase.value = "Esta es una frase de ejemplo para el juego"
                _currentRound.value = 1
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadEndedGames() {
        viewModelScope.launch {
            try {
                val response = gameService.getEndedGames()
                if (response.isSuccessful && response.body() != null) {
                    // Convertir las frases terminadas a CompletedPhrase
                    _completedPhrases.value = response.body()!!.map { phraseResponse ->
                        CompletedPhrase(
                            initialPhrase = "Historia #${phraseResponse.phrase.hashCode().toString().takeLast(4)}", // Identificador único
                            finalPhrase = phraseResponse.phrase,
                            roundsCompleted = phraseResponse.round
                        )
                    }
                } else {
                    Log.w("GameViewModel", "No se pudieron cargar historias finalizadas")
                    // En desarrollo, usar ejemplos
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error al cargar historias finalizadas", e)
                // En desarrollo, usar ejemplos
            }
        }
    }

    fun sendPhrase(userPhrase: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Verificar si tenemos una frase actual para continuar
                if (currentPhraseData == null) {
                    _error.value = "No hay frase actual para continuar"
                    return@launch
                }

                // Crear la petición para añadir la continuación de la historia
                val request = AddPhraseRequest(
                    phrase = userPhrase,
                    round = currentPhraseData!!.round
                )

                val response = gameService.addPhrase(request)
                if (response.isSuccessful) {
                    // Si la historia ha llegado a la ronda 10, actualizar la lista de historias finalizadas
                    if (currentPhraseData!!.round >= 9) {  // 9 + 1 = 10 (ronda final)
                        loadEndedGames()
                    }

                    // Cargar una nueva frase aleatoria para continuar jugando
                    loadRandomPhrase()
                } else {
                    _error.value = "No se pudo enviar la frase: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error al enviar frase", e)
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshGame() {
        loadRandomPhrase()
        loadEndedGames()
    }
}