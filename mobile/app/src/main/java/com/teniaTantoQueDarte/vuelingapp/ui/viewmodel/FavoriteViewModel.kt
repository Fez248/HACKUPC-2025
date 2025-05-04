package com.teniaTantoQueDarte.vuelingapp.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teniaTantoQueDarte.vuelingapp.data.repository.FlightRepository
import com.teniaTantoQueDarte.vuelingapp.model.FlightModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FlightRepository(application)

    private val _favoriteFlights = MutableStateFlow<List<FlightModel>>(emptyList())
    val favoriteFlights: StateFlow<List<FlightModel>> = _favoriteFlights.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadFavoriteFlights()
    }

    private fun loadFavoriteFlights() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Observamos los cambios en favoritos
                repository.observeFavoriteFlights().collectLatest { flights ->
                    _favoriteFlights.value = flights
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error cargando favoritos", e)
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(flightNumber: String, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(flightNumber, isFavorite)
                // No necesitamos actualizar la lista manualmente aquí porque
                // el Flow de observeFavoriteFlights se actualizará automáticamente
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error al actualizar favorito", e)
            }
        }
    }

    // Para forzar una actualización manual
    fun refreshFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _favoriteFlights.value = repository.getFavoriteFlights()
            } finally {
                _isLoading.value = false
            }
        }
    }
}