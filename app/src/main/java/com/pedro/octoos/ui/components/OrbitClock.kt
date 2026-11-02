package com.pedro.octoos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.ShareTechMonoFamily
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Concentric three-ring tick clock in Orbit Mode:
 * - Outer ring: Seconds (60 ticks)
 * - Middle ring: Minutes (60 ticks)
 * - Inner ring: Hours (12 ticks)
 *
 * Each ring rotates so the active value aligns at the top (-90 degrees / 12 o'clock).
 * Center displays HH:MM in Share Tech Mono and the formatted date below in IBM Plex Mono.
 */
@Composable
fun OrbitClock(
    hours24: Int,
    minutes: Int,
    seconds: Int,
    dateLabel: String,
    modifier: Modifier = Modifier,
    ringSize: Dp = 248.dp
) {
    val activeHourIndex = hours24 % 12
    val activeMinuteIndex = minutes.coerceIn(0, 59)
    val activeSecondIndex = seconds.coerceIn(0, 59)
    val timeText = String.format(Locale.ROOT, "%02d:%02d", hours24, minutes)

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val maxRadius = size.minDimension / 2f

            // Outer ring: Seconds (60 ticks)
            drawOrbitRing(
                totalTicks = 60,
                activeIndex = activeSecondIndex,
                outerRadius = maxRadius - 2.dp.toPx(),
                baseTickLength = 7.dp.toPx(),
                majorTickLength = 11.dp.toPx(),
                activeTickLength = 15.dp.toPx(),
                majorEvery = 5
            )

            // Middle ring: Minutes (60 ticks)
            drawOrbitRing(
                totalTicks = 60,
                activeIndex = activeMinuteIndex,
                outerRadius = maxRadius - 24.dp.toPx(),
                baseTickLength = 6.dp.toPx(),
                majorTickLength = 10.dp.toPx(),
                activeTickLength = 14.dp.toPx(),
                majorEvery = 5
            )

            // Inner ring: Hours (12 ticks)
            drawOrbitRing(
                totalTicks = 12,
                activeIndex = activeHourIndex,
                outerRadius = maxRadius - 44.dp.toPx(),
                baseTickLength = 9.dp.toPx(),
                majorTickLength = 12.dp.toPx(),
                activeTickLength = 16.dp.toPx(),
                majorEvery = 3
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeText,
                fontFamily = ShareTechMonoFamily,
                fontSize = 44.sp,
                color = OctoText,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateLabel.uppercase(Locale("pt", "BR")),
                fontFamily = IbmPlexMonoFamily,
                fontSize = 11.sp,
                color = OctoGray,
                letterSpacing = 1.5.sp
            )
        }
    }
}

private fun DrawScope.drawOrbitRing(
    totalTicks: Int,
    activeIndex: Int,
    outerRadius: Float,
    baseTickLength: Float,
    majorTickLength: Float,
    activeTickLength: Float,
    majorEvery: Int
) {
    val stepDegrees = 360f / totalTicks
    for (i in 0 until totalTicks) {
        // Orbit mode rotates the ring by -activeIndex * stepDegrees so i == activeIndex sits at -90 deg (top)
        val relativeSteps = (i - activeIndex + totalTicks) % totalTicks
        val angleDeg = -90f + relativeSteps * stepDegrees
        val angleRad = Math.toRadians(angleDeg.toDouble())

        val isCurrentTop = i == activeIndex
        val isMajor = i % majorEvery == 0

        val tickLength = when {
            isCurrentTop -> activeTickLength
            isMajor -> majorTickLength
            else -> baseTickLength
        }

        val strokeWidth = when {
            isCurrentTop -> 2.6.dp.toPx()
            isMajor -> 1.6.dp.toPx()
            else -> 1.dp.toPx()
        }

        val tickColor: Color = when {
            isCurrentTop -> OctoLime
            isMajor -> OctoGray
            else -> OctoBorder
        }

        val innerRadius = outerRadius - tickLength
        val cosA = cos(angleRad).toFloat()
        val sinA = sin(angleRad).toFloat()

        val start = Offset(
            x = center.x + innerRadius * cosA,
            y = center.y + innerRadius * sinA
        )
        val end = Offset(
            x = center.x + outerRadius * cosA,
            y = center.y + outerRadius * sinA
        )

        drawLine(
            color = tickColor,
            start = start,
            end = end,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Butt
        )
    }
}

fun formatPortugueseShortDate(dateTime: LocalDateTime): String {
    val locale = Locale("pt", "BR")
    val dayOfWeek = dateTime.format(DateTimeFormatter.ofPattern("EEE", locale))
        .trimEnd('.')
        .uppercase(locale)
    val dayMonth = dateTime.format(DateTimeFormatter.ofPattern("dd MMM", locale))
        .replace(".", "")
        .uppercase(locale)
    return "$dayOfWeek · $dayMonth"
}

@Preview(
    name = "OrbitClock — Cyber Brutal",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A
)
@Composable
private fun OrbitClockPreview() {
    OctoOSTheme {
        Box(
            modifier = Modifier
                .background(OctoBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            OrbitClock(
                hours24 = 16,
                minutes = 32,
                seconds = 45,
                dateLabel = "SEG · 02 NOV"
            )
        }
    }
}

