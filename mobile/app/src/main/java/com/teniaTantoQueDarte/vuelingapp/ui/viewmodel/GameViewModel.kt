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
import retrofit2.http.Path
import retrofit2.http.Query

// Datos de ejemplo para frases completadas
val sampleCompletedPhrases = listOf(
    CompletedPhrase(
        initialPhrase = "Un buen libro es aquel que se abre con expectación y se cierra con provecho",
        finalPhrase = "La vida es como un libro que se abre cada día con una página en blanco"
    ),
    CompletedPhrase(
        initialPhrase = "La paciencia es amarga, pero su fruto es dulce",
        finalPhrase = "El tiempo revela la verdad, aunque la paciencia sea un camino difícil"
    ),
    CompletedPhrase(
        initialPhrase = "El conocimiento es poder, pero la sabiduría es saber cómo usarlo",
        finalPhrase = "Quien aprende y no comparte, acumula un tesoro invisible para los demás"
    ),
    CompletedPhrase(
        initialPhrase = "La creatividad es la inteligencia divirtiéndose",
        finalPhrase = "Las mejores ideas surgen cuando dejamos que nuestra mente juegue sin límites"
    )
)

// Modelos de datos para API
data class PhraseResponse(
    val id: String,
    val text: String,
    val currentRound: Int,
    val totalRounds: Int
)

data class CompletedPhraseResponse(
    val id: String,
    val initialPhrase: String,
    val finalPhrase: String,
    val roundsCompleted: Int
)

data class PhraseRequest(
    val text: String,
    val phraseId: String
)

// Interfaz de API
interface GameApiService {
    @GET("game/current-phrase")
    suspend fun getCurrentPhrase(): Response<PhraseResponse>

    @POST("game/send-phrase")
    suspend fun sendPhrase(@Body request: PhraseRequest): Response<PhraseResponse>

    @GET("game/completed-phrases")
    suspend fun getCompletedPhrases(): Response<List<CompletedPhraseResponse>>
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    // Estado UI
    private val _currentPhrase = MutableStateFlow<String>("Cargando frase...")
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

    private val _currentPhraseId = MutableStateFlow("")

    // Servicio API
    private val gameService = RetrofitClient.retrofit.create(GameApiService::class.java)

    init {
        loadCurrentPhrase()
        loadCompletedPhrases()
    }

    private fun loadCurrentPhrase() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val response = gameService.getCurrentPhrase()
                if (response.isSuccessful && response.body() != null) {
                    val phraseData = response.body()!!
                    _currentPhrase.value = phraseData.text
                    _currentRound.value = phraseData.currentRound
                    _totalRounds.value = phraseData.totalRounds
                    _currentPhraseId.value = phraseData.id
                } else {
                    _error.value = "No se pudo cargar la frase: ${response.message()}"
                    // Usar frase de ejemplo para desarrollo
                    _currentPhrase.value = "Esta es una frase de ejemplo para el juego"
                    _currentRound.value = 1
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error al cargar frase", e)
                _error.value = "Error de conexión: ${e.message}"
                // Usar frase de ejemplo para desarrollo
                _currentPhrase.value = "Esta es una frase de ejemplo para el juego"
                _currentRound.value = 1
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadCompletedPhrases() {
        viewModelScope.launch {
            try {
                val response = gameService.getCompletedPhrases()
                if (response.isSuccessful && response.body() != null) {
                    _completedPhrases.value = response.body()!!.map {
                        CompletedPhrase(
                            initialPhrase = it.initialPhrase,
                            finalPhrase = it.finalPhrase,
                            roundsCompleted = it.roundsCompleted
                        )
                    }
                } else {
                    Log.w("GameViewModel", "No se pudieron cargar frases completadas")
                    // En desarrollo, usar ejemplos
                    _completedPhrases.value = sampleCompletedPhrases
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error al cargar historial", e)
                // En desarrollo, usar ejemplos
                _completedPhrases.value = sampleCompletedPhrases
            }
        }
    }

    fun sendPhrase(userPhrase: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val request = PhraseRequest(
                    text = userPhrase,
                    phraseId = _currentPhraseId.value
                )

                val response = gameService.sendPhrase(request)
                if (response.isSuccessful && response.body() != null) {
                    val newPhraseData = response.body()!!
                    _currentPhrase.value = newPhraseData.text
                    _currentRound.value = newPhraseData.currentRound
                    _totalRounds.value = newPhraseData.totalRounds
                    _currentPhraseId.value = newPhraseData.id

                    // Si cambiamos a una nueva ronda, refrescamos las frases completadas
                    if (newPhraseData.currentRound == 1) {
                        loadCompletedPhrases()
                    }
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
        loadCurrentPhrase()
        loadCompletedPhrases()
    }
}