// Actualiza HomeViewmodel.kt
package com.teniaTantoQueDarte.vuelingapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teniaTantoQueDarte.vuelingapp.workers.NetworkSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

class HomeViewmodel(application: Application) : AndroidViewModel(application) {

    // Scope optimizado para tareas en background
    private val backgroundScope = CoroutineScope(
        Dispatchers.Default + SupervisorJob() +
                kotlinx.coroutines.CoroutineName("LowPowerScope")
    )

    init {
        // Programar tareas periódicas
        NetworkSyncWorker.schedulePeriodic(application.applicationContext)
    }

    // Método para realizar operaciones en ventanas de tiempo controladas
    fun performBatchedOperation(operation: suspend () -> Unit) {
        backgroundScope.launch {
            operation()
        }
    }

    override fun onCleared() {
        super.onCleared()
        backgroundScope.coroutineContext.cancelChildren()
    }
}