// Crea NetworkSyncWorker.kt
package com.teniaTantoQueDarte.vuelingapp.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.BackoffPolicy
import androidx.work.NetworkType
import androidx.work.Constraints
import androidx.work.workDataOf
import com.google.gson.Gson
import com.teniaTantoQueDarte.vuelingapp.data.repository.UserRepository
import com.teniaTantoQueDarte.vuelingapp.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import com.teniaTantoQueDarte.vuelingapp.model.FlightModel
import com.teniaTantoQueDarte.vuelingapp.utils.RetrofitClient
import android.util.Log
import com.teniaTantoQueDarte.vuelingapp.services.AdapterRasPi


class NetworkSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        val KEY_NEWS_JSON = "news_json"
        const val SYNC_WORK_NAME = "network_sync_work"
        const val KEY_FLIGHTS_JSON = "flights_json"

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<NetworkSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    SYNC_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("NetworkSyncWorker", "Iniciando sincronización de datos")

            // Retrofit call
            val adapter = AdapterRasPi(applicationContext)
            val response = adapter.getFlights()
            return@withContext response
        } catch (e: Exception) {
            Log.e("NetworkSyncWorker", "Error en sincronización", e)
            Result.retry()
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