package com.pedro.octoos.ui.home

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.pedro.octoos.data.ActiveTripState
import com.pedro.octoos.data.LocationTracker
import com.pedro.octoos.ui.theme.BebasNeueFamily
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoText

/**
 * Rectangular active trip mini-map card shown right below the clock when a trip is active.
 * Renders a real GoogleMap with dark cyber-brutal styling, dashed lime Polyline, and live GPS markers.
 */
@Composable
fun ActiveTripCard(
    trip: ActiveTripState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    val origin = remember(trip.originLat, trip.originLng) {
        LatLng(trip.originLat, trip.originLng)
    }
    val destination = remember(trip.destinationLat, trip.destinationLng) {
        LatLng(trip.destinationLat, trip.destinationLng)
    }
    val routePoints = remember(trip.routePolyline, origin, destination) {
        tripRoutePoints(trip, origin, destination)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OctoBackground)
            .border(1.dp, OctoBorder)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        // Header: VIAGEM EM CURSO | CHEGA 14:21
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VIAGEM EM CURSO",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = OctoLime,
                letterSpacing = 0.6.sp
            )
            Text(
                text = "CHEGA ${trip.arrivalTimeLabel}",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = OctoText,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .background(Color(0xFF0E130D))
                .border(1.dp, OctoBorder)
        ) {
            if (!isPreview) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(origin, 14.5f)
                }
                val mapStyle = remember {
                    MapStyleOptions(LocationTracker.CYBER_BRUTAL_MAP_STYLE_JSON)
                }

                LaunchedEffect(origin, destination) {
                    runCatching {
                        val bounds = LatLngBounds.builder()
                            .include(origin)
                            .include(destination)
                            .build()
                        cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 44))
                    }
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(mapStyleOptions = mapStyle),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        compassEnabled = false,
                        mapToolbarEnabled = false,
                        scrollGesturesEnabled = false,
                        zoomGesturesEnabled = false,
                        tiltGesturesEnabled = false,
                        rotationGesturesEnabled = false
                    ),
                    onMapClick = { onClick() }
                ) {
                    Polyline(
                        points = routePoints,
                        color = OctoLime,
                        width = 7f,
                        pattern = listOf(Dash(18f), Gap(12f))
                    )
                    Circle(
                        center = origin,
                        radius = 45.0,
                        fillColor = Color(0xFF3A86FF),
                        strokeColor = Color.White,
                        strokeWidth = 4f
                    )
                    Circle(
                        center = destination,
                        radius = 35.0,
                        fillColor = OctoLime,
                        strokeColor = OctoBackground,
                        strokeWidth = 2f
                    )
                }
            } else {
                PreviewTacticalMiniMapCanvas()
            }

            // Top-left maneuver distance ("300 M") and top-right remaining time ("12 min")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = trip.nextManeuverDistanceLabel,
                    fontFamily = BebasNeueFamily,
                    fontSize = 18.sp,
                    color = OctoLime,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = trip.remainingDurationLabel,
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 10.sp,
                    color = OctoText
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom maneuver instruction & total distance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Vire à direita · ${trip.destinationAddress}",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = OctoText,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = trip.remainingDistanceKmLabel.replace(" ", "\n"),
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = OctoLime
            )
        }
    }
}

@Composable
private fun PreviewTacticalMiniMapCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val gridColor = Color(0xFF222D21)

        drawLine(gridColor, Offset(0f, h * 0.36f), Offset(w, h * 0.44f), 1.6.dp.toPx())
        drawLine(gridColor, Offset(0f, h * 0.72f), Offset(w, h * 0.70f), 1.6.dp.toPx())
        drawLine(gridColor, Offset(w * 0.31f, 0f), Offset(w * 0.35f, h), 1.6.dp.toPx())
        drawLine(gridColor, Offset(w * 0.68f, 0f), Offset(w * 0.73f, h), 1.6.dp.toPx())

        val routePath = Path().apply {
            moveTo(w * 0.22f, h * 0.72f)
            cubicTo(w * 0.42f, h * 0.45f, w * 0.66f, h * 0.32f, w * 0.94f, h * 0.23f)
        }
        drawPath(
            path = routePath,
            color = OctoLime,
            style = Stroke(
                width = 2.2.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 9f), 0f)
            )
        )
        drawCircle(OctoLime, 3.5.dp.toPx(), Offset(w * 0.94f, h * 0.21f))
        val userCenter = Offset(w * 0.22f, h * 0.72f)
        drawCircle(Color.White, 6.5.dp.toPx(), userCenter)
        drawCircle(Color(0xFF3A86FF), 4.6.dp.toPx(), userCenter)
    }
}

@Preview(
    name = "Viagem em Curso — Card",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A
)
@Composable
private fun ActiveTripCardPreview() {
    OctoOSTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ActiveTripCard(
                trip = ActiveTripState(isActive = true),
                onClick = {}
            )
        }
    }
}

/** Pontos da rota real; sem rota, desenha a linha reta entre origem e destino. */
fun tripRoutePoints(trip: com.pedro.octoos.data.ActiveTripState, origin: com.google.android.gms.maps.model.LatLng, destination: com.google.android.gms.maps.model.LatLng): List<com.google.android.gms.maps.model.LatLng> =
    if (trip.routePolyline.size >= 2) {
        trip.routePolyline.map { com.google.android.gms.maps.model.LatLng(it.first, it.second) }
    } else {
        listOf(origin, destination)
    }
