package com.teniaTantoQueDarte.vuelingapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teniaTantoQueDarte.vuelingapp.data.model.User
import com.teniaTantoQueDarte.vuelingapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class ProfileUiState(
    val points: Int = 0,
    val isSharingMode: Boolean = false,
    val isLoading: Boolean = true
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    fun toggleSharingMode(isSharing: Boolean) {
        viewModelScope.launch {
            repository.toggleSharingMode(isSharing)
            _uiState.update { it.copy(isSharingMode = isSharing) }
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Crear usuario si no existe
            repository.createUserIfNotExists()

            // Observar cambios en el usuario
            repository.getUser().collectLatest { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            points = it.points,
                            isSharingMode = it.isSharingMode,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    // En ProfileViewModel.kt, añade esta función:
    fun performBatchedOperations() {
        // Solo actualizamos si han pasado más de 5 minutos desde la última actualización
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSyncTime > TimeUnit.MINUTES.toMillis(5)) {
            lastSyncTime = currentTime

            viewModelScope.launch {
                // Aquí agrupamos todas las operaciones que requieran red
                //repository.syncAllUserData() // Método que realiza múltiples operaciones en batch
            }
        }
    }

    // Añade esta variable para controlar frecuencia
    private var lastSyncTime = 0L

    // Añade al onCleared():
    override fun onCleared() {
        super.onCleared()
        // Limpieza de recursos
    }
}