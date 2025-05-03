package com.teniaTantoQueDarte.vuelingapp.ui.screen

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import com.teniaTantoQueDarte.vuelingapp.R
import com.teniaTantoQueDarte.vuelingapp.model.FlightModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import com.teniaTantoQueDarte.vuelingapp.ui.theme.VuelingAppTheme
import androidx.compose.ui.graphics.ColorFilter

@Composable
fun FlightItem(item: FlightModel, index: Int){
    val context = LocalContext.current

    ConstraintLayout(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable { }
            .background(
                color = Color(0xFFFFCF31), // Color amarillo básico
                shape = RoundedCornerShape(15.dp)
            )
    ) {
        // El resto del código permanece igual
        val (
            fromTxt, fromShortTxt, departTimeTxt,
            airplaneIcon,
            toTxt, toShortTxt, arriveTimeTxt,
            dashLine,
            flightNumberTxt, statusTxt, reasonTxt
        ) = createRefs()

        // COLUMNA ORIGEN - Izquierda
        Text(
            text = item.From,
            fontSize = 14.sp,
            color = Color(0xFF303336),
            modifier = Modifier.constrainAs(fromTxt) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(parent.start, margin = 16.dp)
            }
        )

        Text(
            text = item.FromShort,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF303336),
            modifier = Modifier.constrainAs(fromShortTxt) {
                top.linkTo(fromTxt.bottom, margin = 4.dp)
                start.linkTo(fromTxt.start)
            }
        )

        Text(
            text = item.DepartTime,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF303336),
            modifier = Modifier.constrainAs(departTimeTxt) {
                top.linkTo(fromShortTxt.bottom, margin = 4.dp)
                start.linkTo(fromTxt.start)
            }
        )

        // ICONO AVIÓN - Centro
        Image(
            painter = painterResource(R.drawable.line_airple_blue),
            contentDescription = null,
            modifier = Modifier
                .size(140.dp) // Añade este modificador para aumentar el tamaño
                .constrainAs(airplaneIcon) {
                    top.linkTo(fromShortTxt.top)
                    bottom.linkTo(fromShortTxt.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background)
        )

        // COLUMNA DESTINO - Derecha
        Text(
            text = item.To,
            fontSize = 14.sp,
            color = Color(0xFF303336),
            modifier = Modifier.constrainAs(toTxt) {
                top.linkTo(parent.top, margin = 16.dp)
                end.linkTo(parent.end, margin = 16.dp)
            }
        )

        Text(
            text = item.ToShort,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF303336),
            modifier = Modifier.constrainAs(toShortTxt) {
                top.linkTo(toTxt.bottom, margin = 4.dp)
                end.linkTo(toTxt.end)
            }
        )

        Text(
            text = item.ArriveTime,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF303336),
            modifier = Modifier.constrainAs(arriveTimeTxt) {
                top.linkTo(toShortTxt.bottom, margin = 4.dp)
                end.linkTo(toTxt.end)
            }
        )

        // LÍNEA DIVISORIA
        Image(
            painter = painterResource(R.drawable.dash_line),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(dashLine) {
                    top.linkTo(departTimeTxt.bottom, margin = 19.dp) // Aumentado de 0.dp a 12.dp
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background)
        )

        // INFORMACIÓN DEL VUELO
        Text(
            text = item.FlightNumber,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF303336),
            modifier = Modifier.constrainAs(flightNumberTxt) {
                top.linkTo(dashLine.bottom, margin = 8.dp)
                start.linkTo(parent.start, margin = 16.dp)
                bottom.linkTo(parent.bottom, margin = 16.dp)
            }
        )

// Status y Reason en columna central (con menos espacio entre ellos)
        Text(
            text = item.Status,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF303336),
            textAlign = TextAlign.Center,
            modifier = Modifier.constrainAs(statusTxt) {
                top.linkTo(dashLine.bottom, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        Text(
            text = item.Reason,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF303336),
            textAlign = TextAlign.Center,
            modifier = Modifier.constrainAs(reasonTxt) {
                top.linkTo(statusTxt.bottom, margin = 0.dp) // Reducido de 4.dp a 2.dp
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom, margin = 10.dp)
            }
        )
    }
}


@Preview
@Composable
fun FlightItemPreview() {
    VuelingAppTheme{
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
                Reason = "Sin retrasos",
                updateTime = "2023-10-01T12:00:00Z"
            ),
            index = 0
        )
    }

}