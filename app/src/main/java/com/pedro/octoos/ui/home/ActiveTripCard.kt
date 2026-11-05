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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.pedro.octoos.data.ActiveTripState
import com.pedro.octoos.ui.theme.BebasNeueFamily
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoText

/**
 * Rectangular active trip mini-map card shown right below the clock when a trip is active.
 * Hidden when no trip is in progress.
 */
@Composable
fun ActiveTripCard(
    trip: ActiveTripState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        // Rectangular mini-map canvas with street grid, dashed lime route, blue GPS dot, 300 M and 12 min
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .background(Color(0xFF0E130D))
                .border(1.dp, OctoBorder)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val gridColor = Color(0xFF222D21)

                // Street grid lines
                drawLine(
                    color = gridColor,
                    start = Offset(0f, h * 0.36f),
                    end = Offset(w, h * 0.44f),
                    strokeWidth = 1.6.dp.toPx()
                )
                drawLine(
                    color = gridColor,
                    start = Offset(0f, h * 0.72f),
                    end = Offset(w, h * 0.70f),
                    strokeWidth = 1.6.dp.toPx()
                )
                drawLine(
                    color = gridColor,
                    start = Offset(w * 0.31f, 0f),
                    end = Offset(w * 0.35f, h),
                    strokeWidth = 1.6.dp.toPx()
                )
                drawLine(
                    color = gridColor,
                    start = Offset(w * 0.68f, 0f),
                    end = Offset(w * 0.73f, h),
                    strokeWidth = 1.6.dp.toPx()
                )

                // Dashed lime maneuver route curve
                val routePath = Path().apply {
                    moveTo(w * 0.22f, h * 0.72f)
                    cubicTo(
                        w * 0.42f, h * 0.45f,
                        w * 0.66f, h * 0.32f,
                        w * 0.94f, h * 0.23f
                    )
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

                // Destination lime square marker at top-right of route
                drawCircle(
                    color = OctoLime,
                    radius = 3.5.dp.toPx(),
                    center = Offset(w * 0.94f, h * 0.21f)
                )

                // Current user GPS dot (white outer ring + blue fill)
                val userCenter = Offset(w * 0.22f, h * 0.72f)
                drawCircle(
                    color = Color.White,
                    radius = 6.5.dp.toPx(),
                    center = userCenter
                )
                drawCircle(
                    color = Color(0xFF3A86FF),
                    radius = 4.6.dp.toPx(),
                    center = userCenter
                )
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
                text = "Vire à direita · Av.\n${trip.destinationAddress.removePrefix("Av. ").trim()}",
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

