package com.teniaTantoQueDarte.vuelingapp.data.repository

import android.content.Context
import com.teniaTantoQueDarte.vuelingapp.data.db.AppDatabase
import com.teniaTantoQueDarte.vuelingapp.data.model.FlightModel as DatabaseFlightModel
import com.teniaTantoQueDarte.vuelingapp.model.FlightModel as UIFlightModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FlightRepository(private val context: Context) {
    private val database by lazy { AppDatabase.getDatabase(context) }
    private val flightDao by lazy { database.flightDao() }

    // Obtener todos los vuelos y convertirlos al modelo de UI
    suspend fun getAllFlights(): List<UIFlightModel> = withContext(Dispatchers.IO) {
        val databaseFlights = flightDao.getAllFlights()
        return@withContext databaseFlights.map { convertToUIModel(it) }
    }

    // Guardar vuelos en la base de datos
    suspend fun insertFlights(flights: List<UIFlightModel>) = withContext(Dispatchers.IO) {
        val databaseFlights = flights.map { convertToDatabaseModel(it) }
        flightDao.insertAll(databaseFlights)
    }

    // Convertir modelo de base de datos a modelo UI
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

    // Convertir modelo UI a modelo de base de datos
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