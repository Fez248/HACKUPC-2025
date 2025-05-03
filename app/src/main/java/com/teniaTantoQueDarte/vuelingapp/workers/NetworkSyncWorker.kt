// Crea NetworkSyncWorker.kt
package com.teniaTantoQueDarte.vuelingapp.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.BackoffPolicy
import com.teniaTantoQueDarte.vuelingapp.data.repository.UserRepository
import com.teniaTantoQueDarte.vuelingapp.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class NetworkSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Aquí realizas las operaciones de red agrupadas
            // Por ejemplo: sincronización de datos, comprobar actualizaciones, etc.

            Result.success()
        } catch (e: Exception) {
            // Implementa backoff exponencial automáticamente
            Result.retry()
        }
    }

    companion object {
        private const val SYNC_WORK_NAME = "network_sync_work"

        // Programa la ejecución periódica
        fun schedulePeriodic(context: Context) {
            val syncRequest = PeriodicWorkRequestBuilder<NetworkSyncWorker>(
                15, TimeUnit.MINUTES,  // Frecuencia mínima
                5, TimeUnit.MINUTES    // Ventana flexible
            )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,  // Backoff exponencial
                    30, TimeUnit.SECONDS        // Duración inicial
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )
        }

        // Cancela el trabajo si es necesario
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
        }
    }
}


// En NetworkSyncWorker.kt
class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = UserRepository(applicationContext)
        return try {
            // Usar PreferenceManager para decidir si sincronizar
            if (shouldSync()) {
                repository.executeInTransaction {
                    repository.updateUserStats()
                    repository.cleanupOldData()
                }
                // Guardar tiempo de sincronización
                PreferenceManager.setLastSyncTime(applicationContext, System.currentTimeMillis())
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun shouldSync(): Boolean {
        val lastSync = PreferenceManager.getLastSyncTime(applicationContext)
        return System.currentTimeMillis() - lastSync > TimeUnit.HOURS.toMillis(2)
    }
}