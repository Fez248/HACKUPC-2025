package com.teniaTantoQueDarte.vuelingapp.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.gson.Gson
import com.teniaTantoQueDarte.vuelingapp.data.db.AppDatabase
import com.teniaTantoQueDarte.vuelingapp.model.FlightModel
import com.teniaTantoQueDarte.vuelingapp.services.AdapterRasPi
import com.teniaTantoQueDarte.vuelingapp.utils.RetrofitClient
import com.teniaTantoQueDarte.vuelingapp.workers.NetworkSyncWorker
import com.teniaTantoQueDarte.vuelingapp.workers.NetworkSyncWorker.Companion.KEY_FLIGHTS_JSON
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.teniaTantoQueDarte.vuelingapp.data.repository.FlightRepository


class HomeViewmodel(application: Application) : AndroidViewModel(application) {

    private val _flights = MutableStateFlow<List<FlightModel>>(emptyList())
    val flights: StateFlow<List<FlightModel>> = _flights.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val gson = Gson()

    private val flightRepository by lazy { FlightRepository(application) }


    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val adapter = AdapterRasPi(application)
    private val workManager = WorkManager.getInstance(application)
    private val liveData: LiveData<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkLiveData(NetworkSyncWorker.SYNC_WORK_NAME)

    private val workObserver = Observer<List<WorkInfo>> { infos ->
        val workInfo = infos.firstOrNull()

        when (workInfo?.state) {
            WorkInfo.State.SUCCEEDED -> {
                val jsonString = workInfo.outputData.getString(NetworkSyncWorker.KEY_FLIGHTS_JSON)
                if (jsonString != null) {
                    processJsonData(jsonString)
                } else {
                    Log.e("HomeViewModel", "JSON recibido es null")
                    _error.value = "No se recibieron datos"
                    _isLoading.value = false
                    loadDataAdapter()
                }
            }
            WorkInfo.State.FAILED -> {
                Log.e("HomeViewModel", "Worker falló")
                _error.value = "No se pudieron cargar los vuelos"
                _isLoading.value = false
                loadDataAdapter()
            }
            WorkInfo.State.RUNNING -> {
                _isLoading.value = true
            }
            else -> {
                // No hacemos nada para otros estados
            }
        }
    }

    init {
        Log.d("HomeViewModel", "Inicializando ViewModel")
        liveData.observeForever(workObserver)

        // Para garantizar que siempre tengamos datos, programamos de inmediato
        scheduleSyncWorkerWithTimeout()
    }

    fun scheduleSyncWorkerWithTimeout() {
        Log.d("HomeViewModel", "Programando sincronización con timeout")
        scheduleSyncWorker()

        viewModelScope.launch {
            delay(500) // Espera 0.5 segundos
            if (_isLoading.value) {
                Log.w("HomeViewModel", "Timeout: Cargando datos por defecto")
                loadDataAdapter() // Carga datos por defecto
                _isLoading.value = false
            }
        }
    }

    fun scheduleSyncWorker() {
        Log.d("HomeViewModel", "Programando sincronización")
        NetworkSyncWorker.schedulePeriodic(getApplication())
    }

    fun refreshFlights() {
        _isLoading.value = true
        _error.value = null
        scheduleSyncWorkerWithTimeout()
    }

    // Función para cargar datos por defecto
    private fun loadDataAdapter() {
        viewModelScope.launch {
            viewModelScope.launch {
                try {
                    // Usamos wrapper para evitar el acceso restringido a Result.Success
                    val adapterResult = adapter.getFlights()

                    // Procesamos el resultado como tipo Any
                    when (adapterResult) {
                        is androidx.work.ListenableWorker.Result -> {
                            // No podemos acceder directamente a Success ni a outputData
                            // Sólo verificamos si es éxito o fallo con toString()
                            val resultString = adapterResult.toString()
                            if (resultString.contains("Success")) {
                                // Esto significa que hay datos en la base de datos
                                // No necesitamos actualizarlos porque AdapterRasPi ya lo hizo
                                // Simplemente cargamos desde DatabaseHelper
                                loadFromDatabase()
                            } else {
                                _error.value = "No se pudieron cargar los vuelos"
                                _isLoading.value = false
                            }
                        }

                        else -> {
                            // Podría ser otro tipo de respuesta
                            _error.value = "Formato de respuesta no esperado"
                            _isLoading.value = false
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error al cargar datos", e)
                    _error.value = "Error: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }

    override fun onCleared() {
        Log.d("HomeViewModel", "ViewModel destruido, eliminando observer")
        liveData.removeObserver(workObserver)
        super.onCleared()
    }

        private fun loadFromDatabase() {
            viewModelScope.launch {
                try {
                    val flights = flightRepository.getAllFlights()

                    if (flights.isNotEmpty()) {
                        _flights.value = flights
                        _error.value = null
                    } else {
                        _error.value = "No hay vuelos disponibles"
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error al cargar desde base de datos", e)
                    _error.value = "Error al cargar datos locales"
                } finally {
                    _isLoading.value = false
                }
            }
        }

    private fun processJsonData(jsonString: String) {
        viewModelScope.launch {
            try {
                val flightsArray = gson.fromJson(jsonString, Array<FlightModel>::class.java)
                _flights.value = flightsArray.toList()
                _error.value = null
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al deserializar JSON", e)
                _error.value = "Error al procesar los datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}