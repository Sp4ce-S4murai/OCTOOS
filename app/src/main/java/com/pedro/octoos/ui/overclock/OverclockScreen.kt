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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.data.OverclockMode
import com.pedro.octoos.ui.theme.ChamferedShape
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoCyan
import com.pedro.octoos.ui.theme.OctoDanger
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.OctoTile
import com.pedro.octoos.ui.theme.OrbitronFamily
import com.pedro.octoos.ui.theme.ShareTechMonoFamily
import kotlin.math.roundToInt

private const val THERMAL_ALERT_THRESHOLD_C = 60

@Composable
fun OverclockScreen(
    selectedMode: OverclockMode,
    cpuPercent: Int,
    ramPercent: Int,
    temperatureCelsius: Int,
    onSelectMode: (OverclockMode) -> Unit,
    onTemperatureOverrideChange: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverheating = temperatureCelsius > THERMAL_ALERT_THRESHOLD_C

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OctoBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OverclockHeader(onBackClick = onBackClick)

        // Thermal alert when temperature exceeds 60°C
        if (isOverheating) {
            ThermalWarningBanner(temperatureCelsius = temperatureCelsius)
        }

        Text(
            text = "MODO DE OPERAÇÃO",
            fontFamily = IbmPlexMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = OctoGray,
            letterSpacing = 1.2.sp
        )

        ModeSelectorRow(
            selectedMode = selectedMode,
            onSelectMode = onSelectMode
        )

        ModeSpecsCard(mode = selectedMode)

        Text(
            text = "TELEMETRIA EM TEMPO REAL",
            fontFamily = IbmPlexMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = OctoGray,
            letterSpacing = 1.2.sp
        )

        SegmentedTelemetryCard(
            label = "CPU",
            valueText = "$cpuPercent%",
            fraction = cpuPercent / 100f,
            activeColor = OctoLime
        )

        SegmentedTelemetryCard(
            label = "RAM",
            valueText = "$ramPercent%",
            fraction = ramPercent / 100f,
            activeColor = OctoCyan
        )

        SegmentedTelemetryCard(
            label = "TEMPERATURA",
            valueText = "$temperatureCelsius°C",
            fraction = ((temperatureCelsius - 25f) / 65f).coerceIn(0.05f, 1f),
            activeColor = if (isOverheating) OctoDanger else OctoLime
        )

        Spacer(modifier = Modifier.weight(1f))

        ThermalSimulatorBox(
            temperatureCelsius = temperatureCelsius,
            onTemperatureChange = onTemperatureOverrideChange
        )
    }
}

@Composable
private fun OverclockHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "OVERCLOCK",
            fontFamily = OrbitronFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = OctoText,
            letterSpacing = 1.5.sp
        )
        Box(
            modifier = Modifier
                .clip(ChamferedShape(8.dp))
                .background(OctoTile)
                .border(1.dp, OctoBorder, ChamferedShape(8.dp))
                .clickable(onClick = onBackClick)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "← VOLTAR",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoLime
            )
        }
    }
}

@Composable
private fun ThermalWarningBanner(temperatureCelsius: Int) {
    val shape = ChamferedShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OctoDanger.copy(alpha = 0.16f))
            .border(1.dp, OctoDanger, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ALERTA TÉRMICO · ACIMA DE 60°C",
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = OctoDanger,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Temperatura crítica ($temperatureCelsius°C). Recomendado reduzir para Eco.",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoText
            )
        }

        Text(
            text = "$temperatureCelsius°C",
            fontFamily = ShareTechMonoFamily,
            fontSize = 20.sp,
            color = OctoDanger
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
            val shape = ChamferedShape(12.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(shape)
                    .background(if (isSelected) OctoLime else OctoTile)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) OctoLime else OctoBorder,
                        shape = shape
                    )
                    .clickable { onSelectMode(mode) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.label.uppercase(),
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isSelected) OctoBackground else OctoText,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun ModeSpecsCard(mode: OverclockMode) {
    val shape = ChamferedShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OctoTile)
            .border(1.dp, OctoBorder, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SpecColumn(
            label = "ANIMAÇÕES",
            value = when (mode) {
                OverclockMode.ECO -> "OFF"
                OverclockMode.EQUILIBRIO -> "NORMAL"
                OverclockMode.OC -> "TURBO"
            }
        )
        SpecColumn(
            label = "PRÉ-CARGA",
            value = "${mode.preloadedAppsCount} APPS"
        )
        SpecColumn(
            label = "RELÓGIO",
            value = "${mode.clockUpdateIntervalMs} MS"
        )
    }
}

@Composable
private fun SpecColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            fontFamily = IbmPlexMonoFamily,
            fontSize = 9.sp,
            color = OctoGray
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = ShareTechMonoFamily,
            fontSize = 14.sp,
            color = OctoLime
        )
    }
}

@Composable
fun SegmentedTelemetryCard(
    label: String,
    valueText: String,
    fraction: Float,
    activeColor: Color,
    totalSegments: Int = 16
) {
    val shape = ChamferedShape(12.dp)
    val litSegments = (fraction.coerceIn(0f, 1f) * totalSegments).roundToInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OctoTile)
            .border(1.dp, OctoBorder, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = OctoText
            )
            Text(
                text = valueText,
                fontFamily = ShareTechMonoFamily,
                fontSize = 16.sp,
                color = activeColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (index in 0 until totalSegments) {
                val isLit = index < litSegments
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .background(if (isLit) activeColor else OctoBackground)
                        .border(
                            width = 1.dp,
                            color = if (isLit) activeColor else OctoBorder
                        )
                )
            }
        }
    }
}

@Composable
private fun ThermalSimulatorBox(
    temperatureCelsius: Int,
    onTemperatureChange: (Int) -> Unit
) {
    val shape = ChamferedShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OctoTile)
            .border(1.dp, OctoBorder, shape)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SIMULADOR TÉRMICO (LIMITE 60°C)",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoGray
            )
            Text(
                text = "$temperatureCelsius°C",
                fontFamily = ShareTechMonoFamily,
                fontSize = 12.sp,
                color = if (temperatureCelsius > THERMAL_ALERT_THRESHOLD_C) OctoDanger else OctoLime
            )
        }
        Slider(
            value = temperatureCelsius.toFloat(),
            onValueChange = { onTemperatureChange(it.roundToInt()) },
            valueRange = 35f..80f,
            colors = SliderDefaults.colors(
                thumbColor = if (temperatureCelsius > THERMAL_ALERT_THRESHOLD_C) OctoDanger else OctoLime,
                activeTrackColor = if (temperatureCelsius > THERMAL_ALERT_THRESHOLD_C) OctoDanger else OctoLime,
                inactiveTrackColor = OctoBorder
            )
        )
    }
}

@Preview(
    name = "Overclock (Equilíbrio) — OctoOS",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun OverclockEquilibrioPreview() {
    OctoOSTheme {
        OverclockScreen(
            selectedMode = OverclockMode.EQUILIBRIO,
            cpuPercent = 42,
            ramPercent = 54,
            temperatureCelsius = 52,
            onSelectMode = {},
            onTemperatureOverrideChange = {},
            onBackClick = {}
        )
    }
}

@Preview(
    name = "Overclock (OC > 60°C Alerta) — OctoOS",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun OverclockAlertPreview() {
    OctoOSTheme {
        OverclockScreen(
            selectedMode = OverclockMode.OC,
            cpuPercent = 88,
            ramPercent = 79,
            temperatureCelsius = 66,
            onSelectMode = {},
            onTemperatureOverrideChange = {},
            onBackClick = {}
        )
    }
}

