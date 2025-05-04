package com.teniaTantoQueDarte.vuelingapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.teniaTantoQueDarte.vuelingapp.model.NewModel
import com.teniaTantoQueDarte.vuelingapp.ui.theme.VuelingAppTheme

@Composable
fun NewTag(item: com.teniaTantoQueDarte.vuelingapp.model.NewModel) {
    // Color de fondo según el FlightNumber
    val backgroundColor = if (item.FlightNumber == "Airport" || item.FlightNumber == "Aeropuerto") {
        Color(0xFFB76C2F)
    } else {
        Color(0xFFDCB12B) // Amarillo como en FlightTicket
    }

    ConstraintLayout(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(15.dp)
            )
            .padding(16.dp)
    ) {
        val (
            flightNumberTxt,
            titleTxt,
            contentTxt,
            updateTimeTxt
        ) = createRefs()

        // FlightNumber en la parte superior
        Text(
            text = item.FlightNumber,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.constrainAs(flightNumberTxt) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
        )

        // Título debajo del FlightNumber
        Text(
            text = item.Title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.constrainAs(titleTxt) {
                top.linkTo(flightNumberTxt.bottom, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        // Contenido de la noticia
        Text(
            text = item.Content,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.constrainAs(contentTxt) {
                top.linkTo(titleTxt.bottom, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        // Update time abajo a la derecha
        Text(
            text = "Update time: ${item.Date}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.constrainAs(updateTimeTxt) {
                top.linkTo(contentTxt.bottom, margin = 8.dp)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
        )
    }
}

@Preview
@Composable
fun NewTagPreview() {
    VuelingAppTheme {

    }
}

@Preview
@Composable
fun NewTagAirportPreview() {
    VuelingAppTheme {


    }
}