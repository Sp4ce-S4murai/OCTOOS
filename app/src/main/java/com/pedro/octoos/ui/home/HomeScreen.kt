package com.pedro.octoos.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedro.octoos.data.ActiveTripState
import com.pedro.octoos.data.AppEntry
import com.pedro.octoos.data.InstalledAppsHelper
import com.pedro.octoos.data.SettingsState
import com.pedro.octoos.data.TelemetryState
import com.pedro.octoos.data.WeatherState
import com.pedro.octoos.ui.components.CyberBackground
import com.pedro.octoos.data.MediaInfo
import com.pedro.octoos.ui.media.NowPlayingBar
import com.pedro.octoos.ui.components.OctoCloudIcon
import com.pedro.octoos.ui.components.OctoSolidIcon
import com.pedro.octoos.ui.components.pressScale
import com.pedro.octoos.ui.components.OrbitClock
import com.pedro.octoos.ui.theme.BebasNeueFamily
import com.pedro.octoos.ui.theme.ChamferedShape
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoDanger
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.OrbitronFamily
import com.pedro.octoos.ui.update.UpdateTopBanner
import com.pedro.octoos.update.UpdateReleaseInfo
import java.util.Locale

@Composable
fun HomeScreen(
    hours24: Int,
    minutes: Int,
    seconds: Int,
    dateLabel: String,
    telemetry: TelemetryState,
    weather: WeatherState,
    gridApps: List<AppEntry>,
    dockApps: List<AppEntry>,
    onAppClick: (AppEntry) -> Unit,
    modifier: Modifier = Modifier,
    settings: SettingsState = SettingsState(),
    activeTrip: ActiveTripState = ActiveTripState(isActive = false),
    pendingUpdate: UpdateReleaseInfo? = null,
    onUpdateBannerDownload: () -> Unit = {},
    onUpdateBannerLater: () -> Unit = {},
    onTripClick: () -> Unit = {},
    onGpsHeaderClick: () -> Unit = {},
    onOpenSettingsClick: () -> Unit = {},
    onOpenOverclockClick: () -> Unit = {},
    onClockClick: () -> Unit = {},
    onWeatherClick: () -> Unit = {},
    nowPlaying: MediaInfo? = null,
    onMediaPlayPause: () -> Unit = {},
    onMediaNext: () -> Unit = {},
    onMediaPrevious: () -> Unit = {}
) {
    // Responsivo: o anel cresce com a largura e a altura reais da tela.
    val config = LocalConfiguration.current
    val screenW = config.screenWidthDp.toFloat()
    val screenH = config.screenHeightDp.toFloat()
    val baseRing = if (activeTrip.isActive) {
        minOf(screenW * 0.62f, screenH * 0.30f).coerceAtMost(230f)
    } else {
        minOf(screenW * 0.86f, screenH * 0.42f).coerceAtMost(340f)
    }
    val scaledRingSize = (baseRing * (settings.clockScalePercent / 100f)).dp

    CyberBackground(
        modifier = modifier.fillMaxSize(),
        animated = settings.animationsEnabled && settings.animatedBackgroundEnabled
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (pendingUpdate != null) {
            UpdateTopBanner(
                releaseInfo = pendingUpdate,
                onDownloadClick = onUpdateBannerDownload,
                onLaterClick = onUpdateBannerLater
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        HomeTelemetryHeader(
            telemetry = telemetry,
            isOffline = weather.isOffline,
            onGpsHeaderClick = onGpsHeaderClick,
            onOpenSettingsClick = onOpenSettingsClick,
            onOpenOverclockClick = onOpenOverclockClick
        )

        Spacer(modifier = Modifier.weight(1f))

        OrbitClock(
            hours24 = hours24,
            minutes = minutes,
            seconds = seconds,
            dateLabel = dateLabel,
            modifier = Modifier.clickable(onClick = onClockClick),
            ringSize = scaledRingSize,
            orbitMode = settings.orbitModeEnabled,
            animationsEnabled = settings.animationsEnabled,
            animationDurationMs = settings.overclockMode.animationDurationMs
        )

        Spacer(modifier = Modifier.height(if (activeTrip.isActive) 12.dp else 18.dp))

        // When a trip is active, show the mini-map right below the clock; otherwise show weather
        if (activeTrip.isActive) {
            ActiveTripCard(
                trip = activeTrip,
                onClick = onTripClick
            )
        } else {
            Box(modifier = Modifier.clickable(onClick = onWeatherClick)) {
                WeatherReadout(weather = weather)
            }
        }

        nowPlaying?.let { info ->
            NowPlayingBar(
                info = info,
                onPlayPause = onMediaPlayPause,
                onNext = onMediaNext,
                onPrevious = onMediaPrevious
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Top app grid with 3.dp usage bars at the base of each tile
        val visibleGridApps = gridApps.take(settings.gridAppsLimit)
        val rows = visibleGridApps.chunked(4)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { rowApps ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowApps.forEach { app ->
                        LimeChamferTile(
                            entry = app,
                            showUsageBar = settings.showUsageBars,
                            onClick = { onAppClick(app) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat((4 - rowApps.size).coerceAtLeast(0)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subtle divider between app grid and dock
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OctoBorder)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4-item Dock (WhatsApp, Câmera, Git, Todos)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            dockApps.take(4).forEach { dockItem ->
                LimeChamferTile(
                    entry = dockItem,
                    showUsageBar = false,
                    onClick = { onAppClick(dockItem) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    }
}

@Composable
private fun HomeTelemetryHeader(
    telemetry: TelemetryState,
    isOffline: Boolean,
    onGpsHeaderClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onOpenOverclockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHot = (telemetry.tempCelsius ?: 0) > 60
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val lat = telemetry.latitude
            val lng = telemetry.longitude
            val coordsText = when {
                !telemetry.isGpsEnabled -> "SEM GPS · ATIVAR"
                lat == null || lng == null -> "GPS SEM SINAL"
                else -> String.format(Locale.US, "X %.4f · Y %.4f", lat, lng)
            }
            Text(
                text = coordsText,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = if (telemetry.isGpsEnabled) OctoLime else OctoGray,
                letterSpacing = 0.4.sp,
                modifier = Modifier.clickable(onClick = onGpsHeaderClick)
            )
            Text(
                text = if (isOffline) "○ OFFLINE" else (telemetry.tempCelsius?.let { "$it°C" } ?: "--°C"),
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = if (isHot && !isOffline) OctoDanger else OctoGray,
                modifier = Modifier.clickable(onClick = onOpenOverclockClick)
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
                    append("OCTO")
                }
                withStyle(SpanStyle(color = OctoLime, fontWeight = FontWeight.Bold)) {
                    append("/OS")
                }
            },
            fontFamily = OrbitronFamily,
            fontSize = 22.sp,
            letterSpacing = 1.5.sp,
            modifier = Modifier.clickable(onClick = onOpenSettingsClick)
        )
    }
}

@Composable
private fun WeatherReadout(
    weather: WeatherState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OctoCloudIcon(size = 16.dp, tint = OctoText)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = OctoLime, fontWeight = FontWeight.Medium)) {
                        append("${weather.temperatureCelsius?.toString() ?: "--"}°  ")
                    }
                    withStyle(SpanStyle(color = OctoGray)) {
                        append("UMID ")
                    }
                    withStyle(SpanStyle(color = OctoLime, fontWeight = FontWeight.Medium)) {
                        append("${weather.humidityPercent?.toString() ?: "--"}%  ")
                    }
                    withStyle(SpanStyle(color = OctoGray)) {
                        append("VENTO ")
                    }
                    withStyle(SpanStyle(color = OctoLime, fontWeight = FontWeight.Medium)) {
                        append("${weather.windKmh?.toString() ?: "--"}")
                    }
                },
                fontFamily = IbmPlexMonoFamily,
                fontSize = 12.sp,
                letterSpacing = 0.6.sp
            )
        }

        if (weather.isOffline) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (weather.hoursSinceUpdate == null) "sem leitura · offline" else "desatualizado · há ${weather.hoursSinceUpdate} h",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 11.sp,
                color = OctoGray,
                letterSpacing = 0.6.sp
            )
        }
    }
}

/**
 * Lime chamfered tile (12.dp top-right cut) with solid black icon, small label inside,
 * and an optional 3.dp usage bar at the bottom proportional to app usage.
 */
@Composable
fun LimeChamferTile(
    entry: AppEntry,
    showUsageBar: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = ChamferedShape(12.dp)
    Box(
        modifier = modifier
            .height(62.dp)
            .pressScale()
            .clip(shape)
            .background(OctoLime)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showUsageBar) 4.dp else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (entry.iconType != null) {
                OctoSolidIcon(
                    type = entry.iconType,
                    size = 20.dp,
                    tint = OctoBackground
                )
            } else {
                Text(
                    text = entry.monogram,
                    fontFamily = BebasNeueFamily,
                    fontSize = 20.sp,
                    color = OctoBackground
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.label,
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
                color = OctoBackground,
                letterSpacing = 0.8.sp,
                maxLines = 1
            )
        }

        if (showUsageBar) {
            val ratio = entry.usageRatio.coerceIn(0.15f, 1f)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 6.dp)
                    .fillMaxWidth(ratio)
                    .height(3.dp)
                    .background(OctoBackground)
            )
        }
    }
}

@Preview(
    name = "Início — OctoOS",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun HomeScreenPreview() {
    OctoOSTheme {
        HomeScreen(
            hours24 = 21,
            minutes = 49,
            seconds = 30,
            dateLabel = "QUA, 04 DE NOV.",
            telemetry = TelemetryState(),
            weather = WeatherState(isOffline = false),
            gridApps = InstalledAppsHelper.defaultHomeGridApps(),
            dockApps = InstalledAppsHelper.defaultDockApps(),
            onAppClick = {}
        )
    }
}

@Preview(
    name = "Início com Viagem — OctoOS",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun HomeScreenTripPreview() {
    OctoOSTheme {
        HomeScreen(
            hours24 = 21,
            minutes = 49,
            seconds = 30,
            dateLabel = "QUA, 04 DE NOV.",
            telemetry = TelemetryState(),
            weather = WeatherState(isOffline = false),
            gridApps = InstalledAppsHelper.defaultHomeGridApps(),
            dockApps = InstalledAppsHelper.defaultDockApps(),
            activeTrip = ActiveTripState(isActive = true),
            onAppClick = {}
        )
    }
}

@Preview(
    name = "Início Offline — OctoOS",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun HomeScreenOfflinePreview() {
    OctoOSTheme {
        HomeScreen(
            hours24 = 21,
            minutes = 49,
            seconds = 30,
            dateLabel = "QUA, 04 DE NOV.",
            telemetry = TelemetryState(),
            weather = WeatherState(isOffline = true, hoursSinceUpdate = 2),
            gridApps = InstalledAppsHelper.defaultHomeGridApps(),
            dockApps = InstalledAppsHelper.defaultDockApps(),
            onAppClick = {}
        )
    }
}
