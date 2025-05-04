package com.teniaTantoQueDarte.vuelingapp.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.teniaTantoQueDarte.vuelingapp.data.db.AppDatabase
import com.teniaTantoQueDarte.vuelingapp.data.model.NewModel as DataNewModel
import com.teniaTantoQueDarte.vuelingapp.model.NewModel as UINewModel
import com.teniaTantoQueDarte.vuelingapp.utils.ThrottleManager
import com.teniaTantoQueDarte.vuelingapp.utils.WakeLockManager
import com.teniaTantoQueDarte.vuelingapp.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class NewRepository(private val context: Context) {
    private val database by lazy { AppDatabase.getDatabase(context) }
    private val newDao by lazy { database.newsDao() }
    private val CACHE_VALIDITY_PERIOD = TimeUnit.MINUTES.toMillis(15)

    // Caché en memoria para reducir lecturas a BD
    @Volatile private var cachedNews: List<DataNewModel>? = null
    private var lastFetchTime = 0L
    private var pendingUpdates = mutableMapOf<String, List<DataNewModel>>()
    private var lastUpdateTime = 0L

    // Convertir de modelo UI a modelo de datos
    private fun convertToDataModel(uiModel: UINewModel): DataNewModel {
        return DataNewModel(
            id = uiModel.id,
            FlightNumber = uiModel.FlightNumber,
            Title = uiModel.Title,
            Content = uiModel.Content,
            Date = uiModel.Date
        )
    }

    // Convertir de modelo de datos a modelo UI
    private fun convertToUIModel(dataModel: DataNewModel): UINewModel {
        return UINewModel(
            id = dataModel.id,
            FlightNumber = dataModel.FlightNumber,
            Title = dataModel.Title,
            Content = dataModel.Content,
            Date = dataModel.Date
        )
    }

    // Obtener todas las noticias como Flow con manejo de errores
    fun observeAllNews(): Flow<List<UINewModel>> {
        return newDao.observeAllNews()
            .map { dataList -> dataList.map { convertToUIModel(it) } }
            .catch { emit(emptyList()) }
            .flowOn(Dispatchers.IO)
    }

    // Obtener todas las noticias como lista
    suspend fun getAllNews(): List<UINewModel> = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()

        // Usar caché si es válida
        if (cachedNews != null && currentTime - lastFetchTime < CACHE_VALIDITY_PERIOD) {
            return@withContext cachedNews!!.map { convertToUIModel(it) }
        }

        // Obtener de BD si la caché expiró
        val news = newDao.getAllNews()
        cachedNews = news
        lastFetchTime = currentTime
        return@withContext news.map { convertToUIModel(it) }
    }

    // Obtener noticias por número de vuelo
    suspend fun getNewsByFlightNumber(flightNumber: String): List<UINewModel> = withContext(Dispatchers.IO) {
        return@withContext newDao.getNewsByFlightNumber(flightNumber).map { convertToUIModel(it) }
    }

    // Insertar noticias en la BD (acepta modelos UI)
    suspend fun insertNews(news: List<UINewModel>) = withContext(Dispatchers.IO) {
        val dataNews = news.map { convertToDataModel(it) }
        queueUpdate("insert", dataNews)
        flushUpdatesIfNeeded()
    }

    // Insertar noticias en la BD (acepta modelos de datos)
    suspend fun insertDataNews(news: List<DataNewModel>) = withContext(Dispatchers.IO) {
        queueUpdate("insert", news)
        flushUpdatesIfNeeded()
    }

    // Eliminar todas las noticias
    suspend fun deleteAllNews() = withContext(Dispatchers.IO) {
        invalidateCache()
        ThrottleManager.throttleOperation("delete_all_news", TimeUnit.HOURS.toMillis(1)) {
            executeInTransaction {
                newDao.deleteAll()
            }
        }
    }

    // Sistema de actualizaciones en lote
    private fun queueUpdate(operation: String, news: List<DataNewModel>) {
        synchronized(pendingUpdates) {
            pendingUpdates[operation] = news
        }
    }

    private suspend fun flushUpdatesIfNeeded() {
        val shouldFlush = synchronized(pendingUpdates) {
            pendingUpdates.isNotEmpty() &&
                    (pendingUpdates.size >= 2 || System.currentTimeMillis() - lastUpdateTime > 5000)
        }

        if (shouldFlush) {
            ThrottleManager.throttleOperation("news_flush", 1000) {
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
            updates.forEach { (operation, newsList) ->
                when(operation) {
                    "insert" -> newDao.insertAll(newsList)
                    // Otros tipos de operaciones
                }
            }
            invalidateCache()
        }
    }

    // Invalidar caché
    private fun invalidateCache() {
        cachedNews = null
        lastFetchTime = 0
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
            val duration = System.currentTimeMillis() - executionStartTime
            if (duration > 1000) {
                // Considerar logging para operaciones largas
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

    // Inicializar con datos de ejemplo
    suspend fun initializeIfEmpty(getSampleNews: () -> List<UINewModel>) = withContext(Dispatchers.IO) {
        val newsCount = newDao.getAllNews().size
        if (newsCount == 0) {
            val dataNews = getSampleNews().map { convertToDataModel(it) }
            insertDataNews(dataNews)
        }
    }

    // Actualizar desde fuente remota
    suspend fun refreshNews(fetchFromRemote: suspend () -> List<DataNewModel>?) {
        ThrottleManager.throttleOperation("refresh_news", CACHE_VALIDITY_PERIOD) {
            withContext(Dispatchers.IO) {
                try {
                    val remoteNews = fetchFromRemote()
                    if (remoteNews != null && remoteNews.isNotEmpty()) {
                        insertDataNews(remoteNews)

                        // Actualizar tiempo de sincronización
                        PreferenceManager.setLastSyncTime(context, System.currentTimeMillis())
                    }
                } catch (e: Exception) {
                    // Manejar error
                }
            }
        }
    }

    // Limpiar noticias antiguas
    suspend fun cleanupOldNews(daysToKeep: Int = 30) {
        ThrottleManager.throttleOperation("cleanup_news", TimeUnit.DAYS.toMillis(1)) {
            executeInTransaction {
                val threshold = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
                try {
                    // Implementar en NewDao un método como:
                    // newDao.deleteOldNews(threshold)
                } catch (e: Exception) {
                    // Manejar error
                }
            }
        }
    }
}