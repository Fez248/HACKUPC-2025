package com.teniaTantoQueDarte.vuelingapp.utils

import android.content.Context
import android.os.PowerManager
import androidx.core.content.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

object WakeLockManager {
    private const val WAKE_LOCK_TAG = "VuelingApp:CriticalOp"
    private const val MAX_LOCAL_DB_OPERATION = 2000L  // Reducido a 2s
    private const val MAX_NETWORK_OPERATION = 10000L  // Reducido a 10s

    suspend fun <T> withLocalWakeLock(context: Context, block: suspend () -> T): T {
        return withOptimizedWakeLock(context, MAX_LOCAL_DB_OPERATION, block)
    }

    suspend fun <T> withNetworkWakeLock(context: Context, block: suspend () -> T): T {
        return withOptimizedWakeLock(context, MAX_NETWORK_OPERATION, block)
    }

    private suspend fun <T> withOptimizedWakeLock(
        context: Context,
        maxDurationMs: Long,
        block: suspend () -> T
    ): T {
        var wakeLock: PowerManager.WakeLock? = null
        val startTime = System.currentTimeMillis()

        try {
            val powerManager = context.getSystemService<PowerManager>() ?:
            return withContext(Dispatchers.IO) { block() }

            // Usar PARTIAL_WAKE_LOCK - mínimo consumo posible
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "$WAKE_LOCK_TAG:${System.currentTimeMillis()}" // Unique tag
            ).apply {
                setReferenceCounted(false)
                acquire(maxDurationMs) // Libera automáticamente después del timeout
            }

            return withContext(Dispatchers.IO) {
                block()
            }
        } finally {
            try {
                // Liberar SIEMPRE y lo antes posible
                wakeLock?.let {
                    if (it.isHeld) {
                        it.release()
                    }
                }
                // Registrar duración para análisis
                val duration = System.currentTimeMillis() - startTime
                if (duration > 1000) {
                    // Considerar logging
                }
            } catch (e: Exception) {
                // Manejar error de liberación
            }
        }
    }
}