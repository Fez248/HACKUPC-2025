package com.teniaTantoQueDarte.vuelingapp.ui.viewmodel

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teniaTantoQueDarte.vuelingapp.data.repository.UserRepository
import com.teniaTantoQueDarte.vuelingapp.services.bluetooth.BTManager
import com.teniaTantoQueDarte.vuelingapp.services.bluetooth.BluetoothConnectionCallback
import com.teniaTantoQueDarte.vuelingapp.utils.PreferenceManager
import com.teniaTantoQueDarte.vuelingapp.utils.WakeLockManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit

data class ProfileUiState(
    val points: Int = 0,
    val isSharingMode: Boolean = false,
    val moreBatteryGuy: Boolean = false,
    val isLoading: Boolean = true
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _permissionsState = MutableStateFlow(false)
    val permissionsState: StateFlow<Boolean> = _permissionsState.asStateFlow()

    // Coroutine específica para operaciones de IO
    private val ioScope = CoroutineScope(
        Dispatchers.IO + SupervisorJob() + CoroutineName("ProfileIO")
    )

    val context = getApplication<Application>().applicationContext

    // Add these to your ProfileViewModel class
    private var lastPointsAddedTime = 0L
    private val POINTS_COOLDOWN_MS = 60000L // 1 minute cooldown

    private var bluetoothManager: BTManager? = null;

    // Control de frecuencia para operaciones batch
    private var lastBatchOperationTime = 0L

    init {
        loadUserData()
    }

    fun hasBluetoothSupport(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    }

    fun setSharingMode(isSharing: Boolean) {
        if (_uiState.value.isSharingMode == isSharing) return
        if (!permissionsState.value) {
            return
        }

        ioScope.launch {
            repository.toggleSharingMode(isSharing)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isSharingMode = isSharing) }
                if (isSharing) {
                    startAddingPointsPeriodically()
                } else {
                    stopAddingPointsPeriodically()
                }
            }
        }
    }

    fun addPointsWithCooldown(points: Int = 5) {
        val currentTime = System.currentTimeMillis()

        // Only add points if cooldown period has passed
        if (currentTime - lastPointsAddedTime > POINTS_COOLDOWN_MS) {
            ioScope.launch {
                // Assuming repository has this method - add it if needed
                repository.addPoints(points)
                lastPointsAddedTime = currentTime
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(points = points) }
                }
            }
        }
    }

    private var pointsJob: Job? = null

    fun startAddingPointsPeriodically(points: Int = 10) {
        if (pointsJob != null) return // Evita múltiples trabajos

        pointsJob = ioScope.launch {
            while (isActive) {
                delay(5 * 10 * 1000) // 5 minutos
                repository.addPoints(points)
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(points = _uiState.value.points + points) }
                }
            }
        }
    }

    fun stopAddingPointsPeriodically() {
        pointsJob?.cancel()
        pointsJob = null
    }


    fun setPermissionsState(isGranted: Boolean) {
        _permissionsState.value = isGranted
    }

    fun setBluetoothManager(manager: BluetoothManager) {
        if (bluetoothManager != null) return
        bluetoothManager = BTManager.getInstance(context, manager)
    }

    fun setBatteryStatus(isMoreBattery: Boolean) {
        // Evita operaciones redundantes
        if (_uiState.value.moreBatteryGuy == isMoreBattery) return

        ioScope.launch {
            repository.setBatteryStatus(isMoreBattery)
            // Actualiza UI en Main dispatcher
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(moreBatteryGuy = isMoreBattery) }
            }
        }
    }

    annotation class ConnectionParams () {}

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    fun startServer(deviceName: String) {
        if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            // Handle missing permission (e.g., log or notify the user)
            return
        }

        bluetoothManager?.startServer(deviceName, object : BluetoothConnectionCallback {
            override fun onDeviceConnected(deviceAddress: String) {
                _uiState.update { it.copy(isSharingMode = true) }
            }

            override fun onDeviceDisconnected(deviceAddress: String) {
                _uiState.update { it.copy(isSharingMode = false) }
            }

            override fun onDataReceived(data: ByteArray) {
                // Handle received data
            }

            override fun onError(errorCode: Int, message: String) {
                // Handle errors
            }
        })
    }

    fun connectTo(deviceName: String) {
        if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Handle missing permission (e.g., log or notify the user)
            return
        }

        bluetoothManager?.connectToServer(deviceName, object : BluetoothConnectionCallback {
            override fun onDeviceConnected(deviceAddress: String) {
                _uiState.update { it.copy(isSharingMode = true) }
            }

            override fun onDeviceDisconnected(deviceAddress: String) {
                _uiState.update { it.copy(isSharingMode = false) }
            }

            override fun onDataReceived(data: ByteArray) {
                // Handle received data
            }

            override fun onError(errorCode: Int, message: String) {
                // Handle errors
            }
        })
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
        // Limpia el BluetoothManager
        bluetoothManager?.cleanup()
    }
}