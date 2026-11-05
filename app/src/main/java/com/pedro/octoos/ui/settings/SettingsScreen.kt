package com.pedro.octoos.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.data.SettingsState
import com.pedro.octoos.ui.theme.BebasNeueFamily
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoAccent
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.components.pressScale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.OctoTile

@Composable
fun SettingsScreen(
    settings: SettingsState,
    isOnline: Boolean,
    onPerformanceFractionChange: (Float) -> Unit,
    onToggleAnimations: (Boolean) -> Unit,
    onToggleAutoUpdate: (Boolean) -> Unit,
    onToggleOfflineMode: (Boolean) -> Unit,
    onAccentChange: (OctoAccent) -> Unit = {},
    onToggleAnimatedBackground: (Boolean) -> Unit = {},
    onOpenOverclockClick: () -> Unit,
    onOpenMapClick: () -> Unit,
    onCheckUpdateClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveOnline = isOnline && !settings.offlineModeEnabled

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(OctoBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        SettingsTopBar(
            isOnline = effectiveOnline,
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionNumberHeader(text = "/01 DESEMPENHO")

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenOverclockClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MODO",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = OctoText,
                letterSpacing = 0.6.sp
            )
            Text(
                text = settings.overclockMode.label,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = OctoLime,
                letterSpacing = 0.6.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rectangular lime slider bar
        CyberBarSlider(
            fraction = settings.performanceSliderFraction,
            onFractionChange = onPerformanceFractionChange
        )

        Spacer(modifier = Modifier.height(22.dp))

        SectionNumberHeader(text = "/02 TEMA")

        Spacer(modifier = Modifier.height(10.dp))

        ThemeSwatchRow(
            selected = settings.accent,
            onSelect = onAccentChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsSwitchRow(
            label = "FUNDO ANIMADO",
            checked = settings.animatedBackgroundEnabled,
            onCheckedChange = onToggleAnimatedBackground
        )

        Spacer(modifier = Modifier.height(22.dp))

        SectionNumberHeader(text = "/03 SISTEMA")

        Spacer(modifier = Modifier.height(8.dp))

        SettingsSwitchRow(
            label = "ANIMAÇÕES",
            checked = settings.animationsEnabled,
            onCheckedChange = onToggleAnimations
        )

        SettingsSwitchRow(
            label = "ATUALIZAR SOZINHO",
            checked = settings.autoUpdateEnabled,
            onCheckedChange = onToggleAutoUpdate
        )

        SettingsSwitchRow(
            label = "MODO OFFLINE",
            checked = settings.offlineModeEnabled,
            onCheckedChange = onToggleOfflineMode,
            invertVisualDirection = true
        )

        SettingsValueRow(
            label = "MAPA",
            value = "GOOGLE MAPS",
            onClick = onOpenMapClick
        )

        SettingsValueRow(
            label = "VERSÃO",
            value = com.pedro.octoos.BuildConfig.VERSION_NAME,
            onClick = {}
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(OctoLime)
                .clickable(onClick = onCheckUpdateClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "VERIFICAR ATUALIZAÇÃO",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoBackground,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun SettingsTopBar(
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
                text = "AJUSTES",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoLime,
                letterSpacing = 0.6.sp,
                modifier = Modifier.clickable(onClick = onBackClick)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (isOnline) OctoLime else OctoGray)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOnline) "ONLINE" else "OFFLINE",
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = OctoGray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OctoBorder)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "AJUSTES",
            fontFamily = BebasNeueFamily,
            fontSize = 36.sp,
            color = OctoLime,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun SectionNumberHeader(text: String) {
    Text(
        text = text,
        fontFamily = IbmPlexMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        color = OctoLime,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun CyberBarSlider(
    fraction: Float,
    onFractionChange: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(OctoTile)
            .border(1.dp, OctoBorder)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newFraction = (offset.x / size.width.toFloat()).coerceIn(0.15f, 1f)
                    onFractionChange(newFraction)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0.15f, 1f))
                .fillMaxHeight()
                .background(OctoLime)
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    invertVisualDirection: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = OctoText,
                letterSpacing = 0.6.sp
            )

            // Rectangular toggle matching mockup
            val isLimeActive = if (invertVisualDirection) checked else checked
            val alignment = when {
                invertVisualDirection && !checked -> Alignment.CenterEnd
                checked -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }

            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(22.dp)
                    .background(OctoBackground)
                    .border(
                        width = 1.dp,
                        color = if (isLimeActive) OctoLime else OctoGray
                    )
                    .padding(3.dp),
                contentAlignment = alignment
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(if (isLimeActive) OctoLime else OctoGray)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OctoBorder)
        )
    }
}

@Composable
private fun SettingsValueRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = OctoText,
                letterSpacing = 0.6.sp
            )
            Text(
                text = value,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoLime,
                letterSpacing = 0.6.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OctoBorder)
        )
    }
}

@Composable
private fun SettingsActionRow(
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = OctoText,
                letterSpacing = 0.6.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OctoBorder)
        )
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
            isOnline = true,
            onPerformanceFractionChange = {},
            onToggleAnimations = {},
            onToggleAutoUpdate = {},
            onToggleOfflineMode = {},
            onOpenOverclockClick = {},
            onOpenMapClick = {},
            onCheckUpdateClick = {},
            onBackClick = {}
        )
    }
}

@Composable
private fun ThemeSwatchRow(
    selected: OctoAccent,
    onSelect: (OctoAccent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OctoAccent.entries.forEach { accent ->
            val isSelected = accent == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(OctoTile)
                    .border(if (isSelected) 2.dp else 1.dp, if (isSelected) accent.color else OctoBorder)
                    .clickable { onSelect(accent) }
                    .pressScale(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(accent.color)
                )
            }
        }
    }
}
