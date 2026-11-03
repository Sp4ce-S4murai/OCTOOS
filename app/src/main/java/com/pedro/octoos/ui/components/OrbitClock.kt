package com.pedro.octoos.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.ShareTechMonoFamily
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/** Raio do mockup em px (anel de 136px). Todas as medidas abaixo são desse mockup, escaladas. */
private const val MOCK_HALF = 68f

private val TrackOff = Color(0xFF161616)
private val ClockCyan = Color(0xFF38F0FF)
private val OrbitEase = CubicBezierEasing(0.3f, 0.8f, 0.2f, 1f)

/**
 * Relógio em órbita, igual ao layout final do OctoOS:
 * - Anel externo: 12 horas (lima)
 * - Anel do meio: 60 minutos (branco)
 * - Anel interno: 60 segundos (ciano, com marca branca no segundo atual)
 *
 * Cada anel gira para que o valor atual fique no topo, com animação suave.
 * No centro: HH:MM em Share Tech Mono e a data embaixo em lima.
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
    val hourValue = hours24 % 12
    val hourAngle = rememberRingAngle(hourValue + minutes / 60f, 12)
    val minuteAngle = rememberRingAngle(minutes + seconds / 60f, 60)
    val secondAngle = rememberRingAngle(seconds.toFloat(), 60)

    val timeText = String.format(Locale.ROOT, "%02d:%02d", hours24, minutes)

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val k = size.minDimension / 2f / MOCK_HALF

            rotate(degrees = hourAngle.value) {
                drawRing(
                    count = 12,
                    litCount = hourValue,
                    headIndex = -1,
                    outerPx = 58f * k,
                    lengthPx = 7f * k,
                    widthPx = 2f * k,
                    litColor = OctoLime,
                    headColor = OctoLime,
                    headLengthPx = 7f * k,
                    glow = true
                )
            }

            rotate(degrees = minuteAngle.value) {
                drawRing(
                    count = 60,
                    litCount = minutes,
                    headIndex = -1,
                    outerPx = 48f * k,
                    lengthPx = 4f * k,
                    widthPx = 1.5f * k,
                    litColor = OctoText,
                    headColor = OctoText,
                    headLengthPx = 4f * k,
                    glow = false
                )
            }

            rotate(degrees = secondAngle.value) {
                drawRing(
                    count = 60,
                    litCount = seconds,
                    headIndex = seconds,
                    outerPx = 36f * k,
                    lengthPx = 4f * k,
                    widthPx = 1f * k,
                    litColor = ClockCyan,
                    headColor = Color.White,
                    headLengthPx = 8f * k,
                    glow = true
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeText,
                fontFamily = ShareTechMonoFamily,
                fontSize = 33.sp,
                color = OctoText
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = dateLabel.uppercase(Locale("pt", "BR")),
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = OctoLime,
                letterSpacing = 1.7.sp
            )
        }
    }
}

/** Ângulo-alvo do anel: o valor atual vai para o topo (girando em sentido anti-horário). */
private fun ringTarget(position: Float, count: Int): Float = -(position / count) * 360f

/**
 * Anima o giro do anel. Quando o valor dá a volta (59 -> 0), o giro continua
 * no mesmo sentido, sem retroceder uma volta inteira.
 */
@Composable
private fun rememberRingAngle(position: Float, count: Int): Animatable<Float, AnimationVector1D> {
    val angle = remember { Animatable(ringTarget(position, count)) }
    LaunchedEffect(position) {
        val goal = ringTarget(position, count)
        val turns = Math.round((angle.value - goal) / 360f)
        angle.animateTo(
            targetValue = goal + turns * 360f,
            animationSpec = tween(durationMillis = 900, easing = OrbitEase)
        )
    }
    return angle
}

private fun DrawScope.drawRing(
    count: Int,
    litCount: Int,
    headIndex: Int,
    outerPx: Float,
    lengthPx: Float,
    widthPx: Float,
    litColor: Color,
    headColor: Color,
    headLengthPx: Float,
    glow: Boolean
) {
    val step = 360f / count
    for (i in 0 until count) {
        val rad = Math.toRadians((-90f + i * step).toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()
        val isHead = i == headIndex
        val isLit = i <= litCount
        val len = if (isHead) headLengthPx else lengthPx
        val start = Offset(center.x + (outerPx - len) * cosA, center.y + (outerPx - len) * sinA)
        val end = Offset(center.x + outerPx * cosA, center.y + outerPx * sinA)
        val color = when {
            isHead -> headColor
            isLit -> litColor
            else -> TrackOff
        }
        if (glow && (isLit || isHead)) {
            drawLine(
                color = color.copy(alpha = 0.28f),
                start = start,
                end = end,
                strokeWidth = widthPx * 3f,
                cap = StrokeCap.Butt
            )
        }
        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = widthPx,
            cap = StrokeCap.Butt
        )
    }
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
                minutes = 35,
                seconds = 0,
                dateLabel = "SEG, 02 DE NOV."
            )
        }
    }
}
