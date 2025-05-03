import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.WorkManager
import com.teniaTantoQueDarte.vuelingapp.workers.NetworkSyncWorker
import java.util.concurrent.TimeUnit

// Ejemplo para NetworkRepository.kt
class NetworkRepository(private val context: Context) {
    // Batched processing - agrupa múltiples operaciones
    suspend fun syncAllData() {
        // En lugar de hacer múltiples llamadas separadas
        // Las agrupamos en una sola operación
       // val data1 = getDataFromApi1()
       // val data2 = getDataFromApi2()

        //updateLocalDatabase(data1, data2)
    }

    // Programa tareas puntuales con exponential backoff
    fun scheduleOneTimeSync() {
        val request = androidx.work.OneTimeWorkRequestBuilder<NetworkSyncWorker>()
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}