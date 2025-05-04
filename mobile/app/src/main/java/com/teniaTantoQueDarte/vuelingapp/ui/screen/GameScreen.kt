package com.teniaTantoQueDarte.vuelingapp.ui.screen

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teniaTantoQueDarte.vuelingapp.ui.viewmodel.GameViewModel

// Modelo de datos para frases completadas
data class CompletedPhrase(
    val initialPhrase: String,
    val finalPhrase: String,
    val roundsCompleted: Int = 10
)

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val currentPhrase = viewModel.currentPhrase.collectAsStateWithLifecycle()
    val currentRound = viewModel.currentRound.collectAsStateWithLifecycle()
    val totalRounds = viewModel.totalRounds.collectAsStateWithLifecycle()
    val completedPhrases = viewModel.completedPhrases.collectAsStateWithLifecycle()
    val isLoading = viewModel.isLoading.collectAsStateWithLifecycle()
    val error = viewModel.error.collectAsStateWithLifecycle()

    var userInput by remember { mutableStateOf("") }
    val maxChars = 50

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Storytelling game!!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Indicador de ronda
        Text(
            text = "Round ${currentRound.value} de ${totalRounds.value}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Frase recibida
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = currentPhrase.value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Campo para escribir la respuesta
        OutlinedTextField(
            value = userInput,
            onValueChange = {
                if (it.length <= maxChars) {
                    userInput = it
                }
            },
            label = { Text("Your contribution...") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                Text("${userInput.length}/$maxChars characters")
            },
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón para enviar
        Button(
            onClick = {
                viewModel.sendPhrase(userInput)
                userInput = ""
            },
            enabled = userInput.isNotBlank() && !isLoading.value,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send")
        }

        if (isLoading.value) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        error.value?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Sección de frases completadas
        if (completedPhrases.value.isNotEmpty()) {
            Spacer(modifier = Modifier.height(40.dp))

            Divider(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp))

            Text(
                text = "Completed stories (10/10 rounds)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Lista de frases completadas
            Column {
                completedPhrases.value.forEach { phrase ->
                    CompletedPhraseItem(completedPhrase = phrase)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CompletedPhraseItem(completedPhrase: CompletedPhrase) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Frase inicial
            Text(
                text = "Story",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = completedPhrase.initialPhrase,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))


            // Badge de rondas completadas
            Row(
                modifier = Modifier.align(Alignment.End)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "${completedPhrase.roundsCompleted}/10 rondas",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

class GameViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

