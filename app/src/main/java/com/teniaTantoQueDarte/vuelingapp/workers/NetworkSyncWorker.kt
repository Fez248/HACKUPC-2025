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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import com.teniaTantoQueDarte.vuelingapp.model.FlightModel
import com.teniaTantoQueDarte.vuelingapp.utils.RetrofitClient

class NetworkSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
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
            // 1️⃣ Retrofit call
            val flights: List<FlightModel> = RetrofitClient.apiService.getFlights().map { apiModel ->
                FlightModel(
                    ArriveTime = apiModel.arrivalTime,
                    DepartTime = apiModel.departureTime,
                    FromShort = apiModel.origin,
                    ToShort = apiModel.destination,
                    Status = apiModel.status,
                    FlightNumber = apiModel.flightNumber,
                    updateTime = System.currentTimeMillis().toString(),
                    favorito = true,
                )
            }

            // 2️⃣ Serialize to JSON
            val json = Gson().toJson(flights)

            // 3️⃣ Return as outputData
            val result = Result.success(workDataOf(KEY_FLIGHTS_JSON to json))

            // Schedule the next execution (for frequent updates)
            schedulePeriodic(applicationContext)

            result
        } catch (e: Exception) {
            // Log the exception for debugging
            e.printStackTrace()
            Result.retry()
        }
    }
}