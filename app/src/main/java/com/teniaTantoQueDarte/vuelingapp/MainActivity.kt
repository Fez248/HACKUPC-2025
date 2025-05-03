package com.teniaTantoQueDarte.vuelingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.teniaTantoQueDarte.vuelingapp.model.FlightModel
import com.teniaTantoQueDarte.vuelingapp.ui.screen.FlightItem
import com.teniaTantoQueDarte.vuelingapp.ui.screen.FlightItemPreview
import com.teniaTantoQueDarte.vuelingapp.ui.theme.VuelingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VuelingAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    innerPadding
                        FlightItem(
                            item = FlightModel(
                                ArriveTime = "12:00",
                                DepartTime = "10:00",
                                Seat = "1A",
                                From = "Barcelona",
                                To = "Madrid",
                                FromShort = "BCN",
                                ToShort = "MAD",
                                FlightNumber = "VY1234",
                                Status = "A tiempo",
                                Reason = "N/A"
                            ),
                            index = 0
                        )

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VuelingAppTheme {
        Greeting("Android")
    }
}