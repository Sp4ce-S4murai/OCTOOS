package com.pedro.octoos.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.pedro.octoos.BuildConfig
import com.pedro.octoos.data.ActiveTripState
import com.pedro.octoos.data.TravelMode
import com.pedro.octoos.ui.theme.BebasNeueFamily
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.OrbitronFamily

@Composable
fun MapScreen(
    activeTrip: ActiveTripState,
    isOnline: Boolean,
    onStartTrip: (String, TravelMode) -> Unit,
    onStopTrip: () -> Unit,
    onOpenGoogleMapsIntent: (String, TravelMode) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var addressQuery by remember { mutableStateOf(activeTrip.destinationAddress) }
    var selectedMode by remember { mutableStateOf(activeTrip.travelMode) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OctoBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        MapTopBar(
            isOnline = isOnline,
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (!isOnline) {
            OfflineMapStateBox(modifier = Modifier.weight(1f))
        } else {
            AddressSearchBar(
                query = addressQuery,
                onQueryChange = { addressQuery = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            TravelModeRow(
                selectedMode = selectedMode,
                onSelectMode = { selectedMode = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RouteMapCanvasOrSdk(
                destinationLabel = addressQuery.ifBlank { "Av. Independência" },
                travelMode = selectedMode,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Primary action: start/stop trip on Home & launch Google Maps by Intent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .background(OctoLime)
                        .clickable {
                            val target = addressQuery.ifBlank { "Av. Independência" }
                            onStartTrip(target, selectedMode)
                            onOpenGoogleMapsIntent(target, selectedMode)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ABRIR GOOGLE MAPS",
                        fontFamily = IbmPlexMonoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = OctoBackground,
                        letterSpacing = 0.8.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .height(46.dp)
                        .background(OctoBackground)
                        .border(1.dp, if (activeTrip.isActive) OctoLime else OctoBorder)
                        .clickable {
                            if (activeTrip.isActive) {
                                onStopTrip()
                            } else {
                                onStartTrip(addressQuery.ifBlank { "Av. Independência" }, selectedMode)
                            }
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (activeTrip.isActive) "PARAR ROTA" else "SIMULAR ROTA",
                        fontFamily = IbmPlexMonoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = if (activeTrip.isActive) OctoLime else OctoText,
                        letterSpacing = 0.6.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MapTopBar(
    isOnline: Boolean,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NAVEGAÇÃO",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoLime,
                letterSpacing = 0.6.sp,
                modifier = Modifier.clickable(onClick = onBackClick)
            )
            Text(
                text = if (isOnline) "GPS ATIVO" else "○ OFFLINE",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoGray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OctoBorder)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = OctoText, fontWeight = FontWeight.Bold)) {
                    append("MAPA")
                }
                withStyle(SpanStyle(color = OctoLime, fontWeight = FontWeight.Bold)) {
                    append("/")
                }
            },
            fontFamily = OrbitronFamily,
            fontSize = 22.sp,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun AddressSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(OctoBackground)
            .border(1.dp, OctoLime)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "> ",
            fontFamily = IbmPlexMonoFamily,
            fontSize = 13.sp,
            color = OctoLime
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = "buscar endereço_",
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 13.sp,
                    color = OctoLime
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                cursorBrush = SolidColor(OctoLime),
                textStyle = TextStyle(
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 13.sp,
                    color = OctoLime
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TravelModeRow(
    selectedMode: TravelMode,
    onSelectMode: (TravelMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TravelMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            Box(
                modifier = Modifier
                    .background(if (isSelected) OctoLime else OctoBackground)
                    .border(1.dp, if (isSelected) OctoLime else OctoBorder)
                    .clickable { onSelectMode(mode) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.label,
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = if (isSelected) OctoBackground else OctoText,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = if (selectedMode == TravelMode.CARRO) "4,2 KM · 12 MIN" else "2,8 KM · 38 MIN",
            fontFamily = IbmPlexMonoFamily,
            fontSize = 10.sp,
            color = OctoGray
        )
    }
}

@Composable
private fun RouteMapCanvasOrSdk(
    destinationLabel: String,
    travelMode: TravelMode,
    modifier: Modifier = Modifier
) {
    val hasValidMapsKey = remember {
        val key = BuildConfig.MAPS_API_KEY
        key.isNotBlank() && key != "DUMMY_LOCAL_KEY"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0E130D))
            .border(1.dp, OctoBorder)
    ) {
        if (hasValidMapsKey) {
            val origin = LatLng(-23.5505, -46.6333)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(origin, 14f)
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isBuildingEnabled = true),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, compassEnabled = false)
            ) {
                Marker(
                    state = MarkerState(position = origin),
                    title = destinationLabel
                )
            }
        } else {
            // Cyber brutal tactical route map canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val gridColor = Color(0xFF222D21)

                for (i in 1..4) {
                    val y = h * (i / 5f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y + 14f),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
                for (i in 1..3) {
                    val x = w * (i / 4f)
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x + 12f, h),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                val routePath = Path().apply {
                    moveTo(w * 0.18f, h * 0.78f)
                    cubicTo(
                        w * 0.36f, h * 0.56f,
                        w * 0.62f, h * 0.42f,
                        w * 0.86f, h * 0.22f
                    )
                }
                drawPath(
                    path = routePath,
                    color = OctoLime,
                    style = Stroke(
                        width = 2.6.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )
                )

                // Destination marker
                drawCircle(
                    color = OctoLime,
                    radius = 5.dp.toPx(),
                    center = Offset(w * 0.86f, h * 0.22f)
                )

                // Origin blue dot with white ring
                val userPos = Offset(w * 0.18f, h * 0.78f)
                drawCircle(color = Color.White, radius = 7.dp.toPx(), center = userPos)
                drawCircle(color = Color(0xFF3A86FF), radius = 5.dp.toPx(), center = userPos)
            }
        }

        // Top overlay with next maneuver distance
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = if (travelMode == TravelMode.CARRO) "300 M" else "120 M",
                fontFamily = BebasNeueFamily,
                fontSize = 24.sp,
                color = OctoLime
            )
            Text(
                text = "DESTINO · ${destinationLabel.uppercase()}",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoText
            )
        }
    }
}

@Composable
private fun OfflineMapStateBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "MAPA\nOFFLINE",
                fontFamily = BebasNeueFamily,
                fontSize = 44.sp,
                lineHeight = 44.sp,
                color = OctoLime,
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Sem conexão com a internet.\nO mapa offline (MapLibre) faz parte da Fase 2.",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = OctoGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(
    name = "Mapa Online — OctoOS",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun MapScreenOnlinePreview() {
    OctoOSTheme {
        MapScreen(
            activeTrip = ActiveTripState(isActive = true),
            isOnline = true,
            onStartTrip = { _, _ -> },
            onStopTrip = {},
            onOpenGoogleMapsIntent = { _, _ -> },
            onBackClick = {}
        )
    }
}

@Preview(
    name = "Mapa Offline — OctoOS",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun MapScreenOfflinePreview() {
    OctoOSTheme {
        MapScreen(
            activeTrip = ActiveTripState(isActive = false),
            isOnline = false,
            onStartTrip = { _, _ -> },
            onStopTrip = {},
            onOpenGoogleMapsIntent = { _, _ -> },
            onBackClick = {}
        )
    }
}

