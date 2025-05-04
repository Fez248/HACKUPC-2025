package com.teniaTantoQueDarte.vuelingapp.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.teniaTantoQueDarte.vuelingapp.data.db.AppDatabase
import com.teniaTantoQueDarte.vuelingapp.data.model.User
import com.teniaTantoQueDarte.vuelingapp.utils.PreferenceManager
import com.teniaTantoQueDarte.vuelingapp.utils.ThrottleManager
import com.teniaTantoQueDarte.vuelingapp.utils.WakeLockManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class UserRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val userDao = database.userDao()
    private val MIN_UPDATE_INTERVAL = TimeUnit.MINUTES.toMillis(15)

    // Cache en memoria para reducir lecturas a BD
    @Volatile private var cachedUser: User? = null
    private var lastUpdateTime = 0L
    private var pendingUpdates = mutableMapOf<String, Any>()

    fun getUser(): Flow<User?> {
        return userDao.getUserById()
            .catch { emit(null) }
            .flowOn(Dispatchers.IO)
    }

    suspend fun createUserIfNotExists() {
        withContext(Dispatchers.IO) {
            val existingUser = userDao.getUserById().firstOrNull()
            if (existingUser == null) {
                userDao.insertUser(User())
            }
        }
    }

    suspend fun toggleSharingMode(isSharing: Boolean) {
        // Utiliza el sistema de batch updates
        queueUpdate("sharing", isSharing)
        flushUpdatesIfNeeded()
    }

    suspend fun addPoints(points: Int) {
        val currentTime = System.currentTimeMillis()
        queueUpdate("points", points)
        flushUpdatesIfNeeded()
    }

    suspend fun setBatteryStatus(isMoreBattery: Boolean) {
        // Utiliza el sistema de batch updates
        queueUpdate("battery", isMoreBattery)
        flushUpdatesIfNeeded()
    }

    private fun queueUpdate(field: String, value: Any) {
        synchronized(pendingUpdates) {
            pendingUpdates[field] = value
        }
    }

    private suspend fun flushUpdatesIfNeeded() {
        val shouldFlush = synchronized(pendingUpdates) {
            pendingUpdates.size >= 3 || System.currentTimeMillis() - lastUpdateTime > 5000
        }

        if (shouldFlush) {
            ThrottleManager.throttleOperation("user_flush", 1000) {
                flushUpdates()
            }
        }
    }

    private suspend fun flushUpdates() {
        val updates = synchronized(pendingUpdates) {
            val copy = pendingUpdates.toMap()
            pendingUpdates.clear()
            copy
        }

        if (updates.isEmpty()) return

        executeInTransaction {
            val user = userDao.getUserById().firstOrNull() ?: return@executeInTransaction
            var updatedUser = user

            // Aplicar todos los cambios pendientes
            updates.forEach { (field, value) ->
                when(field) {
                    "sharing" -> {
                        userDao.updateSharingMode(value as Boolean)
                        updatedUser = updatedUser.copy(isSharingMode = value)
                    }
                    "battery" -> {
                        updatedUser = updatedUser.copy(moreBatteryGuy = value as Boolean)
                    }
                    "points" -> {
                        userDao.addPoints(value as Int)
                        updatedUser = updatedUser.copy(points = user.points + value)
                    }
                }
            }

            // Actualizar timestamp en una sola operación
            userDao.updateUser(updatedUser.copy(lastSync = System.currentTimeMillis()))
        }
    }

    // Versión mejorada para operaciones en transacción
    suspend fun executeInTransaction(operations: suspend () -> Unit) {
        val isCriticalOperation = (System.currentTimeMillis() - lastUpdateTime > 3600000) // 1h
        val executionStartTime = System.currentTimeMillis()

        try {
            if (isCriticalOperation) {
                WakeLockManager.withLocalWakeLock(context) {
                    performTransaction(operations)
                }
            } else {
                performTransaction(operations)
            }
        } finally {
            // Registrar métricas de duración para análisis de batería
            val duration = System.currentTimeMillis() - executionStartTime
            if (duration > 1000) {
                // Considerar logging o telemetría para operaciones largas
            }
            lastUpdateTime = System.currentTimeMillis()
        }
    }

    private suspend fun performTransaction(operations: suspend () -> Unit) {
        withContext(Dispatchers.IO) {
            database.withTransaction {
                operations()
            }
        }
    }

    // En UserRepository.kt
    suspend fun updateUserStats() {
        ThrottleManager.throttleOperation("update_stats", MIN_UPDATE_INTERVAL) {
            withContext(Dispatchers.IO) {
                val user = userDao.getUserById().firstOrNull() ?: return@withContext
                userDao.updateUser(user.copy(lastSync = System.currentTimeMillis()))

                // Actualizar tiempo de sincronización en preferencias
                PreferenceManager.setLastSyncTime(context, System.currentTimeMillis())
            }
        }
    }

    suspend fun cleanupOldData() {
        // Limitar frecuencia a una vez al día
        ThrottleManager.throttleOperation("cleanup", TimeUnit.DAYS.toMillis(1)) {
            withContext(Dispatchers.IO) {
                val threshold = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
                try {
                    userDao.updateOldRecords(threshold, System.currentTimeMillis())
                } catch (e: Exception) {
                    // Manejar error sin consumir recursos excesivos
                }
            }
        }
    }
}