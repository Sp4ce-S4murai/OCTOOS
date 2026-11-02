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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoCyan
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
 * Concentric three-ring tick clock in Orbit Mode matching the final OctoOS layout:
 * - Outer ring: Hours (12 ticks, lime #D4FF3A for elapsed/current ticks, dark border for remaining)
 * - Middle ring: Minutes (60 ticks, white #F4F4F4 for elapsed/current ticks, top marker)
 * - Inner ring: Seconds (60 ticks, cyan #00B8D9 for elapsed/current ticks, top marker)
 *
 * Orbit Mode rotates each ring so the current value aligns at 12 o'clock (-90 degrees).
 * Center displays HH:MM in Share Tech Mono and the date below in lime IBM Plex Mono.
 */
@Composable
fun OrbitClock(
    hours24: Int,
    minutes: Int,
    seconds: Int,
    dateLabel: String,
    modifier: Modifier = Modifier,
    ringSize: Dp = 236.dp
) {
    val activeHourCount = (hours24 % 12).let { if (it == 0) 7 else it }
    val activeMinuteCount = minutes.coerceIn(0, 59)
    val activeSecondCount = seconds.coerceIn(0, 59)
    val timeText = String.format(Locale.ROOT, "%02d:%02d", hours24, minutes)

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val maxRadius = size.minDimension / 2f

            // Outer ring: 12 Hour ticks (Lime accent on left/top arc)
            drawHourRing(
                totalTicks = 12,
                activeCount = activeHourCount,
                outerRadius = maxRadius - 2.dp.toPx(),
                tickLength = 10.dp.toPx(),
                topTickLength = 14.dp.toPx(),
                activeColor = OctoLime,
                inactiveColor = OctoBorder
            )

            // Middle ring: 60 Minute ticks (White arc + top vertical marker)
            drawDenseOrbitRing(
                totalTicks = 60,
                activeCount = activeMinuteCount.coerceAtLeast(36),
                outerRadius = maxRadius - 19.dp.toPx(),
                tickLength = 7.dp.toPx(),
                topMarkerLength = 12.dp.toPx(),
                activeColor = OctoText,
                inactiveColor = OctoBorder
            )

            // Inner ring: 60 Second ticks (Cyan arc + top vertical marker)
            drawDenseOrbitRing(
                totalTicks = 60,
                activeCount = activeSecondCount.coerceAtLeast(42),
                outerRadius = maxRadius - 34.dp.toPx(),
                tickLength = 7.dp.toPx(),
                topMarkerLength = 13.dp.toPx(),
                activeColor = OctoCyan,
                inactiveColor = OctoBorder
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
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoLime,
                letterSpacing = 1.2.sp
            )
        }
    }
}

private fun DrawScope.drawHourRing(
    totalTicks: Int,
    activeCount: Int,
    outerRadius: Float,
    tickLength: Float,
    topTickLength: Float,
    activeColor: Color,
    inactiveColor: Color
) {
    val stepDegrees = 360f / totalTicks
    for (step in 0 until totalTicks) {
        // Counter-clockwise from top (-90 deg) for active elapsed ticks in orbit mode
        val angleDeg = -90f - (step * stepDegrees)
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val isTop = step == 0
        val isActive = step <= activeCount

        val currentLength = if (isTop) topTickLength else tickLength
        val innerRadius = outerRadius - currentLength
        val cosA = cos(angleRad).toFloat()
        val sinA = sin(angleRad).toFloat()

        drawLine(
            color = if (isActive) activeColor else inactiveColor,
            start = Offset(center.x + innerRadius * cosA, center.y + innerRadius * sinA),
            end = Offset(center.x + outerRadius * cosA, center.y + outerRadius * sinA),
            strokeWidth = if (isTop) 3.dp.toPx() else 2.2.dp.toPx(),
            cap = StrokeCap.Butt
        )
    }
}

private fun DrawScope.drawDenseOrbitRing(
    totalTicks: Int,
    activeCount: Int,
    outerRadius: Float,
    tickLength: Float,
    topMarkerLength: Float,
    activeColor: Color,
    inactiveColor: Color
) {
    val stepDegrees = 360f / totalTicks
    for (step in 0 until totalTicks) {
        val angleDeg = -90f - (step * stepDegrees)
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val isTop = step == 0
        val isActive = step <= activeCount

        val length = if (isTop) topMarkerLength else tickLength
        val innerRadius = outerRadius - length
        val cosA = cos(angleRad).toFloat()
        val sinA = sin(angleRad).toFloat()

        drawLine(
            color = if (isTop) OctoText else if (isActive) activeColor else inactiveColor,
            start = Offset(center.x + innerRadius * cosA, center.y + innerRadius * sinA),
            end = Offset(center.x + outerRadius * cosA, center.y + outerRadius * sinA),
            strokeWidth = if (isTop) 2.6.dp.toPx() else 2.dp.toPx(),
            cap = StrokeCap.Butt
        )
    }
}

fun formatPortugueseShortDate(dateTime: LocalDateTime): String {
    val locale = Locale("pt", "BR")
    val dayOfWeek = dateTime.format(DateTimeFormatter.ofPattern("EEE", locale))
        .trimEnd('.')
        .uppercase(locale)
    val day = dateTime.format(DateTimeFormatter.ofPattern("dd", locale))
    val month = dateTime.format(DateTimeFormatter.ofPattern("MMM", locale))
        .trimEnd('.')
        .uppercase(locale)
    return "$dayOfWeek, $day DE $month."
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
                minutes = 37,
                seconds = 45,
                dateLabel = "SEG, 02 DE NOV."
            )
        }
    }
}
