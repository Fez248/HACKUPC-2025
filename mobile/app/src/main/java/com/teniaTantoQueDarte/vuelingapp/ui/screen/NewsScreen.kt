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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teniaTantoQueDarte.vuelingapp.ui.theme.VuelingAppTheme
import com.teniaTantoQueDarte.vuelingapp.ui.viewmodel.NewsViewModel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(viewModel: NewsViewModel) {
    var searchQuery by remember { mutableStateOf("") }

    // Usar noticias del ViewModel si está disponible, o usar datos de ejemplo
    val news = viewModel.news.collectAsState().value

    // Ordenar noticias por fecha (más recientes primero)
    val sortedNews = remember(news) {
        news.sortedByDescending { it.Date }
    }

    // Filtrar por FlightNumber
    val filteredNews = remember(searchQuery, sortedNews) {
        if (searchQuery.isEmpty()) {
            sortedNews
        } else {
            sortedNews.filter { it.FlightNumber.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.loadNews() },  // Asegúrate de tener este método en tu NewsViewModel
                modifier = Modifier.padding(bottom = 16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Recargar noticias",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
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
                placeholder = { Text("Buscar por número de vuelo...") },
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

            // Lista de noticias
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredNews) { newsItem ->
                    NewTag(newsItem)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewsScreenPreview() {
    VuelingAppTheme {
        // NewsScreen() - No se puede usar sin parámetro
    }
}