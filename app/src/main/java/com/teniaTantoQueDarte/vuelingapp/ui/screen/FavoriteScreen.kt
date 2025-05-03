package com.teniaTantoQueDarte.vuelingapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teniaTantoQueDarte.vuelingapp.model.FlightModel
import com.teniaTantoQueDarte.vuelingapp.ui.theme.VuelingAppTheme

// Lista estática temporal de vuelos favoritos
// En una aplicación real, esto vendría de un ViewModel o repositorio
val favoriteFlights = listOf(
    FlightModel(
        ArriveTime = "18:15",
        DepartTime = "15:40",
        //Seat = "23F",
        //From = "Barcelona",
        //To = "Londres",
        FromShort = "BCN",
        ToShort = "LHR",
        Status = "Retrasado",
        //Reason = "Mal tiempo",
        FlightNumber = "VY7842",
        updateTime = "17:45",
        favorito = true
    ),
    FlightModel(
        ArriveTime = "10:20",
        DepartTime = "09:45",
       // Seat = "16D",
       // From = "Valencia",
       // To = "Barcelona",
        FromShort = "VLC",
        ToShort = "BCN",
        Status = "Delayed",
        //Reason = "IDK",
        FlightNumber = "VY3421",
        updateTime = "10:00",
        favorito = true
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val favorites = remember { favoriteFlights }

    // Filtrar vuelos favoritos basado en la búsqueda
    val filteredFavorites = remember(searchQuery, favorites) {
        if (searchQuery.isEmpty()) {
            favorites
        } else {
            favorites.filter { flight ->
                //flight.From.contains(searchQuery, ignoreCase = true) ||
                //flight.To.contains(searchQuery, ignoreCase = true) ||
                flight.FromShort.contains(searchQuery, ignoreCase = true) ||
                flight.ToShort.contains(searchQuery, ignoreCase = true) ||
                flight.FlightNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold { innerPadding ->
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
                placeholder = { Text("Buscar en favoritos...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Icono de búsqueda"
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* Cerrar teclado */ }),
                shape = RoundedCornerShape(25.dp)
            )

            // Lista de vuelos favoritos
            if (filteredFavorites.isEmpty()) {
                Text(
                    text = "No hay vuelos favoritos",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredFavorites) { flight ->
                        FlightItem(
                            item = flight,
                            index = filteredFavorites.indexOf(flight)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoriteScreenPreview() {
    VuelingAppTheme {
        FavoriteScreen()
    }
}