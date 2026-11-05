package com.pedro.octoos.ui.weather

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.data.HourlyPoint
import com.pedro.octoos.data.WeatherRepository
import com.pedro.octoos.data.WeatherState
import com.pedro.octoos.data.fetchToday
import com.pedro.octoos.ui.components.CyberBackground
import com.pedro.octoos.ui.components.pressScale
import com.pedro.octoos.ui.theme.BebasNeueFamily
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoCyan
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.OctoTile
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

@Composable
fun WeatherDetailScreen(
    weather: WeatherState,
    latitude: Double?,
    longitude: Double?,
    isOnline: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    var hourly by remember { mutableStateOf<List<HourlyPoint>?>(null) }

    LaunchedEffect(latitude, longitude, isOnline) {
        if (isOnline && latitude != null && longitude != null) {
            hourly = WeatherRepository.fetchToday(latitude, longitude)
        }
    }

    val currentHour = LocalDateTime.now().hour
    val currentCode = hourly?.firstOrNull { it.hour == currentHour }?.weatherCode
        ?: hourly?.firstOrNull()?.weatherCode
        ?: 0
    val rainMax = hourly?.maxOfOrNull { it.precipitationPercent }

    CyberBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "< VOLTAR",
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = OctoLime,
                    modifier = Modifier
                        .clickable(onClick = onBackClick)
                        .padding(vertical = 6.dp)
                )
                Text(
                    text = "CLIMA · HOJE",
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 11.sp,
                    color = OctoGray
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = weather.temperatureCelsius?.let { "$it°" } ?: "--°",
                        fontFamily = BebasNeueFamily,
                        fontSize = 96.sp,
                        color = OctoLime
                    )
                    Text(
                        text = conditionLabel(currentCode),
                        fontFamily = IbmPlexMonoFamily,
                        fontSize = 12.sp,
                        color = OctoText
                    )
                }
                AnimatedWeatherIcon(code = currentCode, iconSize = 96.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatTile(label = "UMIDADE", value = weather.humidityPercent?.let { "$it%" } ?: "--", modifier = Modifier.weight(1f))
                StatTile(label = "VENTO", value = weather.windKmh?.let { "$it km/h" } ?: "--", modifier = Modifier.weight(1f))
                StatTile(label = "CHUVA MÁX", value = rainMax?.let { "$it%" } ?: "--", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "TEMPERATURA AO LONGO DO DIA",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            val points = hourly
            if (points == null || points.size < 2) {
                Text(
                    text = if (isOnline) "carregando previsão..." else "sem previsão · offline",
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 11.sp,
                    color = OctoGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 60.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                TemperatureChart(points = points, currentHour = currentHour)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(0, 3, 6, 9, 12, 15, 18, 21).forEach { h ->
                        Text(
                            text = String.format("%02d", h),
                            fontFamily = IbmPlexMonoFamily,
                            fontSize = 9.sp,
                            color = OctoGray,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "PRÓXIMAS HORAS",
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 10.sp,
                    color = OctoGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    points.filter { it.hour >= currentHour }.take(12).forEach { p ->
                        HourTile(point = p)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(OctoTile)
            .border(1.dp, OctoBorder)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Text(text = label, fontFamily = IbmPlexMonoFamily, fontSize = 8.sp, color = OctoGray)
        Text(text = value, fontFamily = IbmPlexMonoFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OctoText)
    }
}

@Composable
private fun HourTile(point: HourlyPoint) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .background(OctoTile)
            .border(1.dp, OctoBorder)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = String.format("%02dh", point.hour), fontFamily = IbmPlexMonoFamily, fontSize = 9.sp, color = OctoGray)
        AnimatedWeatherIcon(code = point.weatherCode, iconSize = 28.dp)
        Text(text = "${point.temperatureCelsius}°", fontFamily = IbmPlexMonoFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OctoText)
    }
}

@Composable
private fun TemperatureChart(points: List<HourlyPoint>, currentHour: Int) {
    val lineColor = OctoLime
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(OctoTile)
            .border(1.dp, OctoBorder)
    ) {
        val minT = points.minOf { it.temperatureCelsius }.toFloat()
        val maxT = points.maxOf { it.temperatureCelsius }.toFloat()
        val span = max(maxT - minT, 1f)
        val padX = 14f
        val padTop = 22f
        val padBottom = 14f
        val n = points.size
        val plotW = size.width - padX * 2
        val plotH = size.height - padTop - padBottom

        fun xAt(i: Int): Float = padX + plotW * i / (n - 1).toFloat()
        fun yAt(t: Float): Float = padTop + plotH * (1f - (t - minT) / span)

        // grade horizontal
        for (k in 0..3) {
            val gy = padTop + plotH * k / 3f
            drawLine(lineColor.copy(alpha = 0.08f), Offset(padX, gy), Offset(size.width - padX, gy), strokeWidth = 1f)
        }

        val line = Path()
        val area = Path()
        points.forEachIndexed { i, p ->
            val x = xAt(i)
            val y = yAt(p.temperatureCelsius.toFloat())
            if (i == 0) {
                line.moveTo(x, y)
                area.moveTo(x, size.height - padBottom)
                area.lineTo(x, y)
            } else {
                line.lineTo(x, y)
                area.lineTo(x, y)
            }
        }
        area.lineTo(xAt(n - 1), size.height - padBottom)
        area.close()

        drawPath(
            path = area,
            brush = Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.35f), Color.Transparent))
        )
        drawPath(line, lineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))

        points.forEachIndexed { i, p ->
            val isNow = p.hour == currentHour
            drawCircle(
                color = if (isNow) OctoCyan else lineColor,
                radius = if (isNow) 7f else 3.5f,
                center = Offset(xAt(i), yAt(p.temperatureCelsius.toFloat()))
            )
        }

        val nowIndex = points.indexOfFirst { it.hour == currentHour }
        if (nowIndex >= 0) {
            drawLine(
                color = OctoCyan.copy(alpha = 0.5f),
                start = Offset(xAt(nowIndex), padTop),
                end = Offset(xAt(nowIndex), size.height - padBottom),
                strokeWidth = 1.5f
            )
        }
    }
}

/** Ícone do clima com animação contínua: sol gira, nuvens deslizam, chuva e neve caem, raio pisca. */
@Composable
fun AnimatedWeatherIcon(code: Int, iconSize: Dp, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "weather")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "weatherT"
    )
    Canvas(modifier = modifier.size(iconSize)) {
        when (weatherKind(code)) {
            WeatherKind.SUN -> drawSun(t)
            WeatherKind.CLOUD -> drawCloud(t, OctoText)
            WeatherKind.FOG -> drawFog(t)
            WeatherKind.RAIN -> {
                drawCloud(t, OctoText)
                drawDrops(t, OctoCyan, count = 3)
            }
            WeatherKind.SNOW -> {
                drawCloud(t, OctoText)
                drawDrops(t, OctoText, count = 3, snow = true)
            }
            WeatherKind.STORM -> {
                drawCloud(t, OctoText)
                drawDrops(t, OctoCyan, count = 2)
                if (t < 0.12f) drawCircle(OctoLime, size.width * 0.12f, Offset(size.width * 0.52f, size.height * 0.62f))
            }
        }
    }
}

private enum class WeatherKind { SUN, CLOUD, FOG, RAIN, SNOW, STORM }

private fun weatherKind(code: Int): WeatherKind = when (code) {
    0, 1 -> WeatherKind.SUN
    2, 3 -> WeatherKind.CLOUD
    45, 48 -> WeatherKind.FOG
    71, 73, 75, 77, 85, 86 -> WeatherKind.SNOW
    95, 96, 99 -> WeatherKind.STORM
    else -> WeatherKind.RAIN.takeIf { code in 51..82 } ?: WeatherKind.CLOUD
}

private fun conditionLabel(code: Int): String = when (weatherKind(code)) {
    WeatherKind.SUN -> if (code == 0) "CÉU LIMPO" else "POUCO NUBLADO"
    WeatherKind.CLOUD -> "NUBLADO"
    WeatherKind.FOG -> "NEVOEIRO"
    WeatherKind.RAIN -> "CHUVA"
    WeatherKind.SNOW -> "NEVE"
    WeatherKind.STORM -> "TEMPESTADE"
}

private fun DrawScope.drawSun(t: Float) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = size.width * 0.2f
    drawCircle(OctoLime, r, c)
    val rot = t * 2f * PI.toFloat() / 4f
    for (k in 0 until 8) {
        val a = rot + k * (PI.toFloat() / 4f)
        val inner = size.width * 0.3f
        val outer = size.width * 0.42f
        drawLine(
            OctoLime,
            Offset(c.x + inner * kotlin.math.cos(a), c.y + inner * sin(a)),
            Offset(c.x + outer * kotlin.math.cos(a), c.y + outer * sin(a)),
            strokeWidth = size.width * 0.07f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawCloud(t: Float, color: Color) {
    val drift = sin(t * 2f * PI.toFloat()) * size.width * 0.04f
    val w = size.width
    val h = size.height
    drawCircle(color, w * 0.18f, Offset(w * 0.36f + drift, h * 0.46f))
    drawCircle(color, w * 0.24f, Offset(w * 0.56f + drift, h * 0.40f))
    drawCircle(color, w * 0.16f, Offset(w * 0.74f + drift, h * 0.50f))
    drawRect(color, Offset(w * 0.22f + drift, h * 0.46f), Size(w * 0.6f, h * 0.2f))
}

private fun DrawScope.drawFog(t: Float) {
    val w = size.width
    val h = size.height
    listOf(0.35f, 0.55f, 0.75f).forEachIndexed { i, y ->
        val shift = sin(t * 2f * PI.toFloat() + i) * w * 0.05f
        drawLine(
            OctoGray,
            Offset(w * 0.15f + shift, h * y),
            Offset(w * 0.85f + shift, h * y),
            strokeWidth = w * 0.06f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawDrops(t: Float, color: Color, count: Int, snow: Boolean = false) {
    val w = size.width
    val h = size.height
    for (k in 0 until count) {
        val phase = (t + k / count.toFloat()) % 1f
        val x = w * (0.3f + k * 0.2f)
        val y = h * (0.62f + phase * 0.3f)
        if (snow) {
            drawCircle(color, w * 0.04f, Offset(x, y))
        } else {
            drawLine(color, Offset(x, y), Offset(x - w * 0.03f, y + h * 0.1f), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
        }
    }
}
