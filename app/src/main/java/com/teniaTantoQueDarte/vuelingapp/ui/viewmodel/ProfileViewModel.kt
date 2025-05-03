package com.teniaTantoQueDarte.vuelingapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teniaTantoQueDarte.vuelingapp.data.repository.UserRepository
import com.teniaTantoQueDarte.vuelingapp.utils.PreferenceManager
import com.teniaTantoQueDarte.vuelingapp.utils.WakeLockManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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

    // Coroutine específica para operaciones de IO
    private val ioScope = CoroutineScope(
        Dispatchers.IO + SupervisorJob() + CoroutineName("ProfileIO")
    )

    // Control de frecuencia para operaciones batch
    private var lastBatchOperationTime = 0L

    init {
        // Carga inicial diferida para no bloquear el arranque
        loadUserData()
    }

    fun toggleSharingMode(isSharing: Boolean) {
        // Evita operaciones redundantes
        if (_uiState.value.isSharingMode == isSharing) return

        ioScope.launch {
            repository.toggleSharingMode(isSharing)
            // Actualiza UI en Main dispatcher
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isSharingMode = isSharing) }
            }
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Ejecuta operaciones de DB en dispatcher de IO
            withContext(Dispatchers.IO) {
                // Crear usuario si no existe
                repository.createUserIfNotExists()
            }

            // Observar cambios usando Flow (más eficiente)
            repository.getUser()
                .flowOn(Dispatchers.IO)  // Procesamiento en IO
                .collectLatest { user ->
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

    // Procesa operaciones en batch para minimizar despertares
    // En ProfileViewModel.kt
    fun performBatchDatabaseOperations() {
        val currentTime = System.currentTimeMillis()
        ioScope.launch {
            // Verificar última sincronización
            val lastSync = PreferenceManager.getLastSyncTime(getApplication())
            if (currentTime - lastSync < TimeUnit.MINUTES.toMillis(5)) return@launch

            withWakeLockIfNeeded {
                repository.executeInTransaction {
                    repository.updateUserStats()
                    repository.cleanupOldData()
                }
                // Registrar sincronización
                PreferenceManager.setLastSyncTime(getApplication(), currentTime)
            }
        }
    }

    // Solo usa WakeLock para operaciones realmente críticas de BD
    private suspend fun <T> withWakeLockIfNeeded(block: suspend () -> T): T {
        // Para operaciones críticas de BD (grandes migraciones, etc.)
        if (requiresWakeLock()) {
            return WakeLockManager.withLocalWakeLock(getApplication()) {
                block()
            }
        }
        return block()
    }

    private fun requiresWakeLock(): Boolean {
        // Determina si la operación actual es lo suficientemente crítica
        // para justificar un WakeLock (migraciones, etc.)
        return false // Por defecto desactivado para operaciones de BD locales
    }

    override fun onCleared() {
        super.onCleared()
        // Cancela todas las corrutinas para evitar fugas de memoria
        ioScope.cancel()
    }
}