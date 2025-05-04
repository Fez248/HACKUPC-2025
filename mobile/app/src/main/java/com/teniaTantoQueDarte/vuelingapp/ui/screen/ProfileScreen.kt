package com.teniaTantoQueDarte.vuelingapp.ui.screen

import android.Manifest
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teniaTantoQueDarte.vuelingapp.ui.viewmodel.ProfileViewModel
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    // Optimizamos usando collectAsStateWithLifecycle para mejor rendimiento
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val permissionsState = viewModel.permissionsState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Lanzador de permisos para Bluetooth
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setPermissionsState(true)
        } else {
            viewModel.setPermissionsState(false)
        }
    }

    LaunchedEffect(permissionsState.value) {
        if (!permissionsState.value) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
        }
    }


    // Request bluetooth enable
    val getResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            viewModel.setSharingMode(false)
        }
    }


    LaunchedEffect(uiState.value.isSharingMode) {
        if(!uiState.value.isSharingMode) return@LaunchedEffect
        val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
        if (bluetoothAdapter == null) {
            viewModel.setSharingMode(false)
        }
        else if (bluetoothAdapter.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            getResult.launch(enableBtIntent)
        }
        else {
            viewModel.setBluetoothManager(bluetoothManager)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sección de saludo (simplificada)
        Text(
            text = "Hello!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de puntos
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tus Puntos",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${uiState.value.points}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Switch de modo compartir
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Modo Compartir",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            val isBluetoothEnabled = remember { mutableStateOf(viewModel.hasBluetoothSupport()) }
            Switch(
                checked = uiState.value.isSharingMode,
                onCheckedChange = {
                    if(!isBluetoothEnabled.value) {
                        viewModel.setSharingMode(false)
                        return@Switch
                    }
                    viewModel.setSharingMode(!uiState.value.isSharingMode) }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (uiState.value.isSharingMode) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nivel de batería en tu pareja",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Opción 1: Tengo más batería
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.value.moreBatteryGuy,
                            onClick = { viewModel.setBatteryStatus(true) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Tengo más batería que mi pareja")
                    }

                    // Opción 2: Tengo menos batería
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !uiState.value.moreBatteryGuy,
                            onClick = { viewModel.setBatteryStatus(false) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Tengo menos batería que mi pareja")
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Sección para código de 6 dígitos - diferente según nivel de batería
                    if (uiState.value.moreBatteryGuy) {
                        // Usuario con más batería: Mostrar código de 6 dígitos
                        val code = remember {
                            (100000..999999).random().toString()
                        }
                        val serverStarted = remember { mutableStateOf(false) }

                        Text(
                            text = "Tu código de sincronización:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = code,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                        Text(
                            text = "Comparte este código con tu pareja",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.BLUETOOTH_ADVERTISE
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    serverStarted.value = true
                                    viewModel.startServer(code)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.BLUETOOTH_ADVERTISE)
                                }
                            },
                            enabled = !serverStarted.value,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sincronizar")
                        }
                    } else {
                        // Usuario con menos batería: Mostrar campo para introducir código
                        val inputCodeState = remember { mutableStateOf("") }

                        Text(
                            text = "Introduce el código de tu pareja:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputCodeState.value,
                            onValueChange = { newValue: String ->
                                // Limitar a 6 dígitos y solo números
                                if (newValue.length <= 6 && newValue.all { char: Char -> char.isDigit() }) {
                                    inputCodeState.value = newValue
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.BLUETOOTH_SCAN
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    viewModel.connectTo(inputCodeState.value.toString())
                                } else {
                                    permissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
                                }
                            },
                            enabled = inputCodeState.value.length == 6,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sincronizar")
                        }
                    }
                }
            }
        }
    }
}

// Factory correcta para AndroidViewModel
class ProfileViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


private data class ProfileDisplayData(
    val formattedPoints: String,
    val isSharing: Boolean
)


@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}

