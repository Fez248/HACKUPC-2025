package com.teniaTantoQueDarte.vuelingapp.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Utilidad para limitar la frecuencia de operaciones
 */
object ThrottleManager {
    private val operationTimestamps = ConcurrentHashMap<String, Long>()
    private val mutex = Mutex()

    /**
     * Ejecuta una operación solo si ha pasado el tiempo mínimo desde la última ejecución
     */
    suspend fun <T> throttleOperation(
        key: String,
        minIntervalMs: Long,
        operation: suspend () -> T
    ): T? {
        mutex.withLock {
            val lastExecutionTime = operationTimestamps[key] ?: 0L
            val now = System.currentTimeMillis()

            return if (now - lastExecutionTime >= minIntervalMs) {
                operationTimestamps[key] = now
                operation()
            } else {
                null
            }
        }
    }
}