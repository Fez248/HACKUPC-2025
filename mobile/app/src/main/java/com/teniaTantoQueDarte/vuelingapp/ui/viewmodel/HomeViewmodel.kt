// Actualiza HomeViewmodel.kt
package com.teniaTantoQueDarte.vuelingapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.gson.Gson
import com.teniaTantoQueDarte.vuelingapp.model.FlightModel
import com.teniaTantoQueDarte.vuelingapp.workers.NetworkSyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewmodel(application: Application) : AndroidViewModel(application) {

    private val _flights = MutableStateFlow<List<FlightModel>>(emptyList())
    val flights: StateFlow<List<FlightModel>> = _flights.asStateFlow()

    private val workManager = WorkManager.getInstance(application)
    private val liveData: LiveData<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkLiveData(NetworkSyncWorker.SYNC_WORK_NAME)

    // Observer para recoger el outputData de cada ejecución
    private val workObserver = Observer<List<WorkInfo>> { infos ->
        infos
            ?.firstOrNull { it.state.isFinished }
            ?.outputData
            ?.getString(NetworkSyncWorker.KEY_FLIGHTS_JSON)
            ?.let { json ->
                // --- Deserializamos directamente a Array<FlightModel> ---
                val flightsArray = Gson().fromJson(json, Array<FlightModel>::class.java)
                _flights.value = flightsArray.toList()
            }
    }

    init {
        // Programar tareas periódicas
        NetworkSyncWorker.schedulePeriodic(application.applicationContext)
        liveData.observeForever(workObserver)
    }

    override fun onCleared() {
        liveData.removeObserver(workObserver)
        super.onCleared()
    }
}