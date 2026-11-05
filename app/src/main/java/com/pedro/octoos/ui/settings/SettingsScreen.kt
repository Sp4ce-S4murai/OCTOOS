package com.pedro.octoos.ui.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.BuildConfig
import com.pedro.octoos.data.SettingsState
import com.pedro.octoos.ui.theme.ChamferedShape
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.OctoTile
import com.pedro.octoos.ui.theme.OrbitronFamily
import com.pedro.octoos.ui.theme.ShareTechMonoFamily
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    settings: SettingsState,
    updateStatusMessage: String?,
    onToggleOrbitMode: (Boolean) -> Unit,
    onToggleUsageBars: (Boolean) -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onClockScaleChange: (Int) -> Unit,
    onGridLimitChange: (Int) -> Unit,
    onOpenOverclockClick: () -> Unit,
    onCheckUpdateClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OctoBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsHeader(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(4.dp))

        SectionLabel(text = "INTERFACE E RELÓGIO")

        CyberSwitchControlRow(
            title = "MODO ÓRBITA",
            subtitle = "Gira os anéis para alinhar o horário atual no topo",
            checked = settings.orbitModeEnabled,
            onCheckedChange = onToggleOrbitMode
        )

        CyberSwitchControlRow(
            title = "BARRA DE USO NOS TILES",
            subtitle = "Exibe linha de 3dp proporcional às aberturas",
            checked = settings.showUsageBars,
            onCheckedChange = onToggleUsageBars
        )

        CyberSwitchControlRow(
            title = "RESPOSTA TÁTIL",
            subtitle = "Vibração curta ao abrir apps e controles",
            checked = settings.hapticFeedback,
            onCheckedChange = onToggleHaptic
        )

        CyberSliderControlRow(
            title = "ESCALA DO RELÓGIO",
            valueLabel = "${settings.clockScalePercent}%",
            value = settings.clockScalePercent.toFloat(),
            valueRange = 80f..120f,
            steps = 7,
            onValueChange = { onClockScaleChange(it.roundToInt()) }
        )

        CyberSliderControlRow(
            title = "GRADE DA TELA INICIAL",
            valueLabel = "${settings.gridAppsLimit} APPS",
            value = settings.gridAppsLimit.toFloat(),
            valueRange = 4f..8f,
            steps = 3,
            onValueChange = { onGridLimitChange(it.roundToInt()) }
        )

        Spacer(modifier = Modifier.height(4.dp))

        SectionLabel(text = "DESEMPENHO E SISTEMA")

        OverclockShortcutRow(
            modeLabel = settings.overclockMode.label.uppercase(),
            cpuTemp = settings.effectiveCpuTemp,
            preloadCount = settings.overclockMode.preloadedAppsCount,
            onClick = onOpenOverclockClick
        )

        Spacer(modifier = Modifier.height(6.dp))

        CheckUpdateButton(
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            statusMessage = updateStatusMessage,
            onClick = onCheckUpdateClick
        )
    }
}

@Composable
private fun SettingsHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "CONFIGURAÇÕES",
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
                text = "← INÍCIO",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoLime
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = IbmPlexMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        color = OctoGray,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun CyberSwitchControlRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val shape = ChamferedShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OctoTile)
            .border(1.dp, OctoBorder, shape)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = OctoText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoGray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Value on the right + cyber switch pill
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (checked) "ON" else "OFF",
                fontFamily = ShareTechMonoFamily,
                fontSize = 13.sp,
                color = if (checked) OctoLime else OctoGray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(22.dp)
                    .clip(ChamferedShape(6.dp))
                    .background(if (checked) OctoLime else OctoBackground)
                    .border(
                        width = 1.dp,
                        color = if (checked) OctoLime else OctoBorder,
                        shape = ChamferedShape(6.dp)
                    )
                    .padding(3.dp),
                contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(14.dp)
                        .background(if (checked) OctoBackground else OctoGray)
                )
            }
        }
    }
}

@Composable
private fun CyberSliderControlRow(
    title: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    val shape = ChamferedShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OctoTile)
            .border(1.dp, OctoBorder, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = OctoText
            )
            Text(
                text = valueLabel,
                fontFamily = ShareTechMonoFamily,
                fontSize = 15.sp,
                color = OctoLime
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = OctoLime,
                activeTrackColor = OctoLime,
                inactiveTrackColor = OctoBorder,
                activeTickColor = OctoBackground,
                inactiveTickColor = OctoGray
            )
        )
    }
}

@Composable
private fun OverclockShortcutRow(
    modeLabel: String,
    cpuTemp: Int,
    preloadCount: Int,
    onClick: () -> Unit
) {
    val shape = ChamferedShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OctoTile)
            .border(1.dp, OctoLime.copy(alpha = 0.55f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "PAINEL DE OVERCLOCK",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = OctoText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$preloadCount APPS PRÉ-CARREGADOS · $cpuTemp°C",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoGray
            )
        }

        Text(
            text = "$modeLabel →",
            fontFamily = ShareTechMonoFamily,
            fontSize = 15.sp,
            color = OctoLime
        )
    }
}

@Composable
private fun CheckUpdateButton(
    versionName: String,
    versionCode: Int,
    statusMessage: String?,
    onClick: () -> Unit
) {
    val shape = ChamferedShape(12.dp)
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(shape)
                .background(OctoLime)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "VERIFICAR ATUALIZAÇÃO",
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = OctoBackground,
                letterSpacing = 1.5.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "VERSÃO ATUAL: v$versionName ($versionCode)",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoGray
            )
            if (!statusMessage.isNullOrBlank()) {
                Text(
                    text = statusMessage,
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 10.sp,
                    color = OctoLime
                )
            }
        }
    }
}

@Preview(
    name = "Configurações — OctoOS",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun SettingsScreenPreview() {
    OctoOSTheme {
        SettingsScreen(
            settings = SettingsState(),
            updateStatusMessage = "SISTEMA ATUALIZADO",
            onToggleOrbitMode = {},
            onToggleUsageBars = {},
            onToggleHaptic = {},
            onClockScaleChange = {},
            onGridLimitChange = {},
            onOpenOverclockClick = {},
            onCheckUpdateClick = {},
            onBackClick = {}
        )
    }
}

