package com.teniaTantoQueDarte.vuelingapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teniaTantoQueDarte.vuelingapp.model.FlightModel
import com.teniaTantoQueDarte.vuelingapp.ui.theme.VuelingAppTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.collectAsState
import com.teniaTantoQueDarte.vuelingapp.ui.viewmodel.HomeViewmodel

fun getSampleFlights(): List<FlightModel> {
    return listOf(
        FlightModel(
            ArriveTime = "12:45",
            DepartTime = "11:30",
            FromShort = "BCN",
            ToShort = "MAD",
            Status = "A tiempo",
            FlightNumber = "VY1235",
            updateTime = "12:00",
            favorito = true
        ),
        FlightModel(
            ArriveTime = "18:15",
            DepartTime = "15:40",
            FromShort = "BCN",
            ToShort = "LHR",
            Status = "Retrasado",
            FlightNumber = "VY7842",
            updateTime = "17:45",
            favorito = true
        ),
        FlightModel(
            ArriveTime = "21:00",
            DepartTime = "19:45",
            FromShort = "MAD",
            ToShort = "FCO",
            Status = "Cancelado",
            FlightNumber = "VY6574",
            updateTime = "20:00",
            favorito = false
        ),
        FlightModel(
            ArriveTime = "10:20",
            DepartTime = "09:45",
            FromShort = "VLC",
            ToShort = "BCN",
            Status = "Delayed",
            FlightNumber = "VY3421",
            updateTime = "10:00",
            favorito = false
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewmodel: HomeViewmodel) {
    var searchQuery by remember { mutableStateOf("") }
    val flights: List<FlightModel> by viewmodel.flights.collectAsState(emptyList())

    val filteredFlights: List<FlightModel> = remember(searchQuery, flights) {
        if (searchQuery.isEmpty()) {
            flights
        } else {
            flights.filter { flight ->
                flight.FromShort.contains(searchQuery, ignoreCase = true) ||
                        flight.ToShort.contains(searchQuery, ignoreCase = true) ||
                        flight.FlightNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar vuelos...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Icono de búsqueda"
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* Cerrar teclado */ }),
                shape = RoundedCornerShape(25.dp) // Añade bordes redondeados
            )

            // Lista de vuelos
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredFlights) { flight ->
                    FlightItem(
                        item = flight,
                        index = filteredFlights.indexOf(flight),
                        onFavoriteClick = { flightNumber, isFavorite ->
                            viewmodel.toggleFavorite(flightNumber, isFavorite)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    VuelingAppTheme {
        //HomeScreen()
    }
}