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
}