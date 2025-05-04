package com.teniaTantoQueDarte.vuelingapp.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.gson.Gson
import com.teniaTantoQueDarte.vuelingapp.data.repository.NewRepository
import com.teniaTantoQueDarte.vuelingapp.model.NewModel
import com.teniaTantoQueDarte.vuelingapp.services.AdapterRasPi
import com.teniaTantoQueDarte.vuelingapp.workers.NetworkSyncWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val _news = MutableStateFlow<List<NewModel>>(emptyList())
    val news: StateFlow<List<NewModel>> = _news.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val gson = Gson()
    private val adapter = AdapterRasPi(application)
    private val newsRepository by lazy { NewRepository(application) }

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
                    Log.e("NewsViewModel", "JSON recibido es null")
                    _error.value = "No se recibieron datos"
                    _isLoading.value = false
                    loadDataAdapter()
                }
            }
            WorkInfo.State.FAILED -> {
                Log.e("NewsViewModel", "Worker falló")
                _error.value = "No se pudieron cargar las noticias"
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
        Log.d("NewsViewModel", "Inicializando ViewModel")
        liveData.observeForever(workObserver)

        // Para garantizar que siempre tengamos datos
        scheduleSyncWorkerWithTimeout()
    }

    fun loadNews() {
        _isLoading.value = true
        _error.value = null
        scheduleSyncWorkerWithTimeout()
    }

    private fun scheduleSyncWorkerWithTimeout() {
        Log.d("NewsViewModel", "Programando sincronización con timeout")
        NetworkSyncWorker.schedulePeriodic(getApplication())

        viewModelScope.launch {
            delay(500) // Espera 0.5 segundos
            if (_isLoading.value) {
                Log.w("NewsViewModel", "Timeout: Cargando datos por defecto")
                loadDataAdapter() // Carga datos por defecto
                _isLoading.value = false
            }
        }
    }

    private fun loadDataAdapter() {
        viewModelScope.launch {
            try {
                val adapterResult = adapter.getNews()

                when (adapterResult) {
                    is androidx.work.ListenableWorker.Result -> {
                        val resultString = adapterResult.toString()
                        if (resultString.contains("Success")) {
                            loadFromDatabase()
                        } else {
                            _error.value = "No se pudieron cargar las noticias"
                            _isLoading.value = false
                        }
                    }
                    else -> {
                        _error.value = "Formato de respuesta no esperado"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e("NewsViewModel", "Error al cargar datos", e)
                _error.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun loadFromDatabase() {
        viewModelScope.launch {
            try {
                val newsFromDb = newsRepository.getAllNews()

                if (newsFromDb.isNotEmpty()) {
                    _news.value = newsFromDb
                    _error.value = null
                } else {
                    _error.value = "No hay noticias disponibles"
                }
            } catch (e: Exception) {
                Log.e("NewsViewModel", "Error al cargar desde base de datos", e)
                _error.value = "Error al cargar datos locales"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun processJsonData(jsonString: String) {
        viewModelScope.launch {
            try {
                val newsArray = gson.fromJson(jsonString, Array<NewModel>::class.java)
                _news.value = newsArray.toList()
                _error.value = null
            } catch (e: Exception) {
                Log.e("NewsViewModel", "Error al deserializar JSON", e)
                _error.value = "Error al procesar los datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshNews() {
        _isLoading.value = true
        _error.value = null
        scheduleSyncWorkerWithTimeout()
    }

    override fun onCleared() {
        Log.d("NewsViewModel", "ViewModel destruido, eliminando observer")
        liveData.removeObserver(workObserver)
        super.onCleared()
    }
}