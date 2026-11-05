package com.pedro.octoos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.pedro.octoos.data.ActiveTripState
import com.pedro.octoos.data.InstalledAppsHelper
import com.pedro.octoos.data.LauncherPreferencesRepository
import com.pedro.octoos.data.SettingsState
import com.pedro.octoos.data.TelemetryState
import com.pedro.octoos.data.WeatherState
import com.pedro.octoos.ui.allapps.AllAppsScreen
import com.pedro.octoos.ui.components.OctoIconType
import com.pedro.octoos.ui.components.formatPortugueseShortDate
import com.pedro.octoos.ui.home.HomeScreen
import com.pedro.octoos.ui.map.MapScreen
import com.pedro.octoos.ui.overclock.OverclockScreen
import com.pedro.octoos.ui.settings.SettingsScreen
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoOSTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

enum class OctoRoute {
    HOME,
    ALL_APPS,
    MAP,
    SETTINGS,
    OVERCLOCK
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OctoOSTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OctoBackground),
                    containerColor = OctoBackground
                ) { innerPadding ->
                    LauncherRoot(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun LauncherRoot(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { LauncherPreferencesRepository(context) }

    var currentRoute by remember { mutableStateOf(OctoRoute.HOME) }

    val baseGridApps = remember { InstalledAppsHelper.defaultHomeGridApps() }
    val dockApps = remember { InstalledAppsHelper.defaultDockApps() }
    val allCatalogApps = remember { InstalledAppsHelper.loadAllInstalledApps(context) }

    val trackedPackages = remember(allCatalogApps, baseGridApps) {
        (allCatalogApps.map { it.packageName } + baseGridApps.map { it.packageName }).distinct()
    }

    val launchCounts by prefsRepo.launchCountsFlow(trackedPackages)
        .collectAsState(initial = emptyMap())
    val storedWeather by prefsRepo.weatherFlow
        .collectAsState(initial = WeatherState())
    val storedSettings by prefsRepo.settingsFlow
        .collectAsState(initial = SettingsState())
    val activeTrip by prefsRepo.activeTripFlow
        .collectAsState(initial = ActiveTripState())

    val tickIntervalMs = storedSettings.overclockMode.clockUpdateIntervalMs

    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var networkConnected by remember { mutableStateOf(InstalledAppsHelper.isNetworkConnected(context)) }
    val effectiveOnline = networkConnected && !storedSettings.offlineModeEnabled

    // Clock update loop frequency controlled by OverclockMode (Eco 5000ms, Equilíbrio 1000ms, OC 250ms)
    LaunchedEffect(tickIntervalMs) {
        while (true) {
            now = LocalDateTime.now()
            networkConnected = InstalledAppsHelper.isNetworkConnected(context)
            delay(tickIntervalMs)
        }
    }

    val maxCount = (launchCounts.values.maxOrNull() ?: 42).coerceAtLeast(1)

    val allAppsWithUsage = allCatalogApps.map { app ->
        val count = launchCounts[app.packageName] ?: app.launchCount
        app.copy(
            launchCount = count,
            usageRatio = (count.toFloat() / maxCount.toFloat()).coerceIn(0.15f, 1f)
        )
    }

    val gridCandidateApps = remember(baseGridApps, allAppsWithUsage, storedSettings.overclockMode, launchCounts) {
        val preloadedPool = (baseGridApps + allAppsWithUsage)
            .distinctBy { it.packageName }
            .take(storedSettings.overclockMode.preloadedAppsCount)
        preloadedPool.map { app ->
            val count = launchCounts[app.packageName] ?: app.launchCount
            app.copy(
                launchCount = count,
                usageRatio = (count.toFloat() / maxCount.toFloat()).coerceIn(0.25f, 1f)
            )
        }
    }

    BackHandler(enabled = currentRoute != OctoRoute.HOME) {
        currentRoute = OctoRoute.HOME
    }

    when (currentRoute) {
        OctoRoute.HOME -> {
            HomeScreen(
                hours24 = now.hour,
                minutes = now.minute,
                seconds = now.second,
                dateLabel = formatPortugueseShortDate(now),
                telemetry = TelemetryState(cpuTempCelsius = storedSettings.effectiveCpuTemp),
                weather = storedWeather.copy(isOffline = !effectiveOnline),
                gridApps = gridCandidateApps,
                dockApps = dockApps,
                settings = storedSettings,
                activeTrip = activeTrip,
                onTripClick = { currentRoute = OctoRoute.MAP },
                onOpenSettingsClick = { currentRoute = OctoRoute.SETTINGS },
                onOpenOverclockClick = { currentRoute = OctoRoute.OVERCLOCK },
                onAppClick = { entry ->
                    when {
                        entry.iconType == OctoIconType.TODOS ||
                            entry.packageName == "octo.internal.all_apps" -> {
                            currentRoute = OctoRoute.ALL_APPS
                        }

                        entry.iconType == OctoIconType.MAPA || entry.label == "MAPA" -> {
                            scope.launch { prefsRepo.incrementLaunchCount(entry.packageName) }
                            currentRoute = OctoRoute.MAP
                        }

                        entry.label == "CONFIG" -> {
                            currentRoute = OctoRoute.SETTINGS
                        }

                        else -> {
                            scope.launch {
                                prefsRepo.incrementLaunchCount(entry.packageName)
                            }
                            InstalledAppsHelper.launchAppOrFallback(context, entry)
                        }
                    }
                },
                modifier = modifier
            )
        }

        OctoRoute.ALL_APPS -> {
            AllAppsScreen(
                apps = allAppsWithUsage,
                dockApps = dockApps,
                onAppClick = { entry ->
                    when {
                        entry.label == "CONFIG" -> currentRoute = OctoRoute.SETTINGS
                        entry.iconType == OctoIconType.MAPA || entry.label == "MAPA" -> {
                            scope.launch { prefsRepo.incrementLaunchCount(entry.packageName) }
                            currentRoute = OctoRoute.MAP
                        }
                        else -> {
                            scope.launch {
                                prefsRepo.incrementLaunchCount(entry.packageName)
                            }
                            InstalledAppsHelper.launchAppOrFallback(context, entry)
                        }
                    }
                },
                onBackClick = { currentRoute = OctoRoute.HOME },
                modifier = modifier
            )
        }

        OctoRoute.MAP -> {
            MapScreen(
                activeTrip = activeTrip,
                isOnline = effectiveOnline,
                onStartTrip = { destination, mode ->
                    scope.launch { prefsRepo.startTrip(destination, mode) }
                },
                onStopTrip = {
                    scope.launch { prefsRepo.stopTrip() }
                },
                onOpenGoogleMapsIntent = { destination, mode ->
                    InstalledAppsHelper.launchGoogleMapsNavigation(context, destination, mode)
                },
                onBackClick = { currentRoute = OctoRoute.HOME },
                modifier = modifier
            )
        }

        OctoRoute.SETTINGS -> {
            SettingsScreen(
                settings = storedSettings,
                isOnline = networkConnected,
                onPerformanceFractionChange = { fraction ->
                    scope.launch { prefsRepo.setPerformanceSliderFraction(fraction) }
                },
                onToggleAnimations = { enabled ->
                    scope.launch { prefsRepo.setAnimationsEnabled(enabled) }
                },
                onToggleAutoUpdate = { enabled ->
                    scope.launch { prefsRepo.setAutoUpdateEnabled(enabled) }
                },
                onToggleOfflineMode = { enabled ->
                    scope.launch { prefsRepo.setOfflineModeEnabled(enabled) }
                },
                onOpenOverclockClick = { currentRoute = OctoRoute.OVERCLOCK },
                onOpenMapClick = { currentRoute = OctoRoute.MAP },
                onCheckUpdateClick = {
                    // Connects to Task 6 Update screen
                },
                onBackClick = { currentRoute = OctoRoute.HOME },
                modifier = modifier
            )
        }

        OctoRoute.OVERCLOCK -> {
            OverclockScreen(
                selectedMode = storedSettings.overclockMode,
                cpuPercent = storedSettings.overclockMode.baseCpuPercent,
                ramPercent = storedSettings.overclockMode.baseRamPercent,
                temperatureCelsius = storedSettings.effectiveCpuTemp,
                onSelectMode = { mode ->
                    scope.launch { prefsRepo.setOverclockMode(mode) }
                },
                onBackClick = { currentRoute = OctoRoute.SETTINGS },
                modifier = modifier
            )
        }
    }
}
