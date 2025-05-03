package com.teniaTantoQueDarte.vuelingapp.data.repository

import android.content.Context
import com.teniaTantoQueDarte.vuelingapp.data.db.AppDatabase
import com.teniaTantoQueDarte.vuelingapp.data.model.FlightModel as DatabaseFlightModel
import com.teniaTantoQueDarte.vuelingapp.model.FlightModel as UIFlightModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FlightRepository(private val context: Context) {
    private val database by lazy { AppDatabase.getDatabase(context) }
    private val flightDao by lazy { database.flightDao() }

    // Obtener todos los vuelos
    suspend fun getAllFlights(): List<UIFlightModel> = withContext(Dispatchers.IO) {
        val databaseFlights = flightDao.getAllFlights()
        return@withContext databaseFlights.map { convertToUIModel(it) }
    }

    // Obtener solo los vuelos favoritos
    suspend fun getFavoriteFlights(): List<UIFlightModel> = withContext(Dispatchers.IO) {
        val favoriteFlights = flightDao.getFavoriteFlights()
        return@withContext favoriteFlights.map { convertToUIModel(it) }
    }

    // Observar vuelos favoritos con Flow para actualizaciones en tiempo real
    fun observeFavoriteFlights(): Flow<List<UIFlightModel>> {
        return flightDao.observeFavoriteFlights().map { flights ->
            flights.map { convertToUIModel(it) }
        }
    }

    // Actualizar el estado favorito de un vuelo
    suspend fun toggleFavorite(flightNumber: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        flightDao.updateFavoriteStatus(flightNumber, isFavorite)
    }

    // Guardar vuelos en la base de datos
    suspend fun insertFlights(flights: List<UIFlightModel>) = withContext(Dispatchers.IO) {
        val databaseFlights = flights.map { convertToDatabaseModel(it) }
        flightDao.insertAll(databaseFlights)
    }

    // Funciones de conversión entre modelos
    private fun convertToUIModel(flight: DatabaseFlightModel): UIFlightModel {
        return UIFlightModel(
            ArriveTime = flight.ArriveTime,
            DepartTime = flight.DepartTime,
            FromShort = flight.FromShort,
            ToShort = flight.ToShort,
            Status = flight.Status,
            FlightNumber = flight.FlightNumber,
            updateTime = flight.updateTime,
            favorito = flight.favorito
        )
    }

    private fun convertToDatabaseModel(flight: UIFlightModel): DatabaseFlightModel {
        return DatabaseFlightModel(
            ArriveTime = flight.ArriveTime,
            DepartTime = flight.DepartTime,
            FromShort = flight.FromShort,
            ToShort = flight.ToShort,
            Status = flight.Status,
            FlightNumber = flight.FlightNumber,
            updateTime = flight.updateTime,
            favorito = flight.favorito
        )
    }
}