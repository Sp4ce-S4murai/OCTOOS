package com.pedro.octoos.ui.overclock

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.data.OverclockMode
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoTile
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.OrbitronFamily
import kotlin.math.roundToInt

private const val THERMAL_ALERT_THRESHOLD_C = 60
private val PeakSegmentOrange = Color(0xFFFF5722)

/** Valores nulos aparecem como "--" e as barras ficam apagadas. */
@Composable
fun OverclockScreen(
    selectedMode: OverclockMode,
    cpuPercent: Int?,
    ramPercent: Int?,
    temperatureCelsius: Int?,
    onSelectMode: (OverclockMode) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverheating = (temperatureCelsius ?: 0) > THERMAL_ALERT_THRESHOLD_C

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OctoBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        OverclockTopBar(
            selectedMode = selectedMode,
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        SegmentedBarSection(
            label = "CPU",
            valueLabel = cpuPercent?.let { "$it%" } ?: "--",
            litSegments = cpuPercent?.let { segmentsFor(it, 100) } ?: 0,
            peakStartSegment = 8
        )

        Spacer(modifier = Modifier.height(18.dp))

        SegmentedBarSection(
            label = "RAM",
            valueLabel = ramPercent?.let { "$it%" } ?: "--",
            litSegments = ramPercent?.let { segmentsFor(it, 100) } ?: 0,
            peakStartSegment = 10
        )

        Spacer(modifier = Modifier.height(18.dp))

        SegmentedBarSection(
            label = "TEMPERATURA",
            valueLabel = temperatureCelsius?.let { "$it°C" } ?: "--°C",
            litSegments = temperatureCelsius?.let { segmentsFor(it, 100) } ?: 0,
            peakStartSegment = 6
        )

        Spacer(modifier = Modifier.weight(1f))

        if (isOverheating) {
            ThermalWarningBox()
            Spacer(modifier = Modifier.height(14.dp))
        }

        ModeSelectorRow(
            selectedMode = selectedMode,
            onSelectMode = onSelectMode
        )
    }
}

/** Converte um valor de 0 a [max] em 0 a 10 segmentos. */
private fun segmentsFor(value: Int, max: Int): Int =
    ((value.coerceIn(0, max) / max.toFloat()) * 10).roundToInt()

@Composable
private fun OverclockTopBar(
    selectedMode: OverclockMode,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OVERCLOCK",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoLime,
                letterSpacing = 0.6.sp,
                modifier = Modifier.clickable(onClick = onBackClick)
            )
            Text(
                text = "MODO ${selectedMode.shortChipLabel}",
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
                    append("DESEMPENHO")
                }
                withStyle(SpanStyle(color = OctoLime, fontWeight = FontWeight.Bold)) {
                    append("/")
                }
            },
            fontFamily = OrbitronFamily,
            fontSize = 20.sp,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
private fun SegmentedBarSection(
    label: String,
    valueLabel: String,
    litSegments: Int,
    peakStartSegment: Int,
    totalSegments: Int = 10
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoLime,
                letterSpacing = 0.8.sp
            )
            Text(
                text = valueLabel,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoText,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (index in 0 until totalSegments) {
                val isLit = index < litSegments
                val segmentColor = when {
                    !isLit -> OctoTile
                    index >= peakStartSegment -> PeakSegmentOrange
                    else -> OctoLime
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .background(segmentColor)
                )
            }
        }
    }
}

@Composable
private fun ThermalWarningBox() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OctoBackground)
            .border(1.dp, OctoBorder)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Text(
            text = "AQUECENDO",
            fontFamily = IbmPlexMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = OctoText,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Temperatura acima de 60°C.\nVolte ao modo equilibrado.",
            fontFamily = IbmPlexMonoFamily,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = OctoGray
        )
    }
}

@Composable
private fun ModeSelectorRow(
    selectedMode: OverclockMode,
    onSelectMode: (OverclockMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OverclockMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(if (isSelected) OctoLime else OctoBackground)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) OctoLime else OctoBorder
                    )
                    .clickable { onSelectMode(mode) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.shortChipLabel,
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = if (isSelected) OctoBackground else OctoText,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Preview(
    name = "Overclock — OctoOS",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun OverclockPreview() {
    OctoOSTheme {
        OverclockScreen(
            selectedMode = OverclockMode.OC,
            cpuPercent = 90,
            ramPercent = 50,
            temperatureCelsius = 66,
            onSelectMode = {},
            onBackClick = {}
        )
    }
}
