package com.pedro.octoos

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.LatLng
import com.pedro.octoos.data.ActiveTripState
import com.pedro.octoos.data.InstalledAppsHelper
import com.pedro.octoos.data.LauncherPreferencesRepository
import com.pedro.octoos.data.LocationTracker
import com.pedro.octoos.data.MediaSessionRepository
import com.pedro.octoos.data.SettingsState
import com.pedro.octoos.data.SystemSnapshot
import com.pedro.octoos.data.SystemTelemetry
import com.pedro.octoos.data.TelemetryState
import com.pedro.octoos.data.WeatherRepository
import com.pedro.octoos.data.WeatherState
import com.pedro.octoos.ui.allapps.AllAppsScreen
import com.pedro.octoos.ui.clock.ClockScreen
import com.pedro.octoos.ui.components.OctoIconType
import com.pedro.octoos.ui.components.formatPortugueseShortDate
import com.pedro.octoos.ui.home.HomeScreen
import com.pedro.octoos.ui.lock.LockScreenActivity
import com.pedro.octoos.ui.map.MapScreen
import com.pedro.octoos.ui.overclock.OverclockScreen
import com.pedro.octoos.ui.settings.SettingsScreen
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoPalette
import com.pedro.octoos.ui.update.UpdateScreen
import com.pedro.octoos.ui.weather.WeatherDetailScreen
import com.pedro.octoos.update.GithubUpdateManager
import com.pedro.octoos.update.UpdateCheckResult
import com.pedro.octoos.update.UpdateReleaseInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

enum class OctoRoute {
    HOME,
    ALL_APPS,
    MAP,
    SETTINGS,
    OVERCLOCK,
    UPDATE,
    CLOCK,
    WEATHER
}

private const val WEATHER_REFRESH_MS = 30 * 60_000L
private const val WEATHER_CHECK_EVERY_MS = 60_000L
private const val SYSTEM_SAMPLE_MS = 2_000L

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
    var availableUpdate by remember { mutableStateOf<UpdateReleaseInfo?>(null) }
    var bannerDismissed by remember { mutableStateOf(false) }
    var downloadProgressPercent by remember { mutableIntStateOf(0) }

    var hasLocationPermission by remember {
        mutableStateOf(LocationTracker.hasLocationPermission(context))
    }
    var isGpsEnabled by remember {
        mutableStateOf(LocationTracker.isGpsProviderEnabled(context))
    }
    var liveLocation by remember {
        mutableStateOf<LatLng?>(LocationTracker.readCurrentLocation(context))
    }
    var system by remember { mutableStateOf(SystemSnapshot(null, null, null)) }
    var networkConnected by remember { mutableStateOf(InstalledAppsHelper.isNetworkConnected(context)) }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var mediaAccess by remember { mutableStateOf(false) }
    val nowPlaying by MediaSessionRepository.state.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        isGpsEnabled = LocationTracker.isGpsProviderEnabled(context)
        if (granted) {
            liveLocation = LocationTracker.readCurrentLocation(context)
        }
    }

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

    LaunchedEffect(storedSettings.accent) {
        OctoPalette.accent = storedSettings.accent
    }

    // Primeiro início: pede localização e, em seguida, a permissão de instalar atualizações.
    val onboardingDone by prefsRepo.onboardingDoneFlow
        .collectAsState(initial = true)

    val onboardingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        isGpsEnabled = LocationTracker.isGpsProviderEnabled(context)
        if (granted) {
            liveLocation = LocationTracker.readCurrentLocation(context)
        }
        scope.launch { prefsRepo.markOnboardingDone() }
        if (!GithubUpdateManager.canInstallUnknownPackages(context)) {
            GithubUpdateManager.openUnknownSourcesPermissionSettings(context)
        }
    }

    LaunchedEffect(onboardingDone) {
        if (!onboardingDone) {
            onboardingLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    val activeTrip by prefsRepo.activeTripFlow
        .collectAsState(initial = ActiveTripState())

    val tickIntervalMs = storedSettings.overclockMode.clockUpdateIntervalMs
    val effectiveOnline = networkConnected && !storedSettings.offlineModeEnabled
    val latestLocation by rememberUpdatedState(liveLocation)

    // Atualização: consulta ao abrir (online), respeitando "ATUALIZAR SOZINHO"
    LaunchedEffect(effectiveOnline, storedSettings.autoUpdateEnabled) {
        if (effectiveOnline && storedSettings.autoUpdateEnabled && availableUpdate == null) {
            val result = GithubUpdateManager.checkLatestRelease()
            if (result is UpdateCheckResult.Available) {
                availableUpdate = result.info
            }
        }
    }

    // Relógio, rede e GPS no ritmo do modo de overclock
    LaunchedEffect(tickIntervalMs) {
        while (true) {
            now = LocalDateTime.now()
            networkConnected = InstalledAppsHelper.isNetworkConnected(context)
            mediaAccess = MediaSessionRepository.isListenerEnabled(context)
            hasLocationPermission = LocationTracker.hasLocationPermission(context)
            isGpsEnabled = LocationTracker.isGpsProviderEnabled(context)
            if (hasLocationPermission && isGpsEnabled) {
                LocationTracker.readCurrentLocation(context)?.let { liveLocation = it }
            }
            delay(tickIntervalMs)
        }
    }

    // Temperatura, CPU e RAM reais
    LaunchedEffect(Unit) {
        while (true) {
            system = withContext(Dispatchers.IO) { SystemTelemetry.read(context) }
            delay(SYSTEM_SAMPLE_MS)
        }
    }

    // Clima real, atualizado a cada 30 min quando há internet e localização
    LaunchedEffect(effectiveOnline) {
        var lastFetchMs = 0L
        while (true) {
            val loc = latestLocation
            val due = System.currentTimeMillis() - lastFetchMs > WEATHER_REFRESH_MS
            if (effectiveOnline && loc != null && due) {
                val reading = withContext(Dispatchers.IO) {
                    WeatherRepository.fetchCurrent(loc.latitude, loc.longitude)
                }
                if (reading != null) {
                    prefsRepo.saveWeather(reading)
                    lastFetchMs = System.currentTimeMillis()
                }
            }
            delay(WEATHER_CHECK_EVERY_MS)
        }
    }

    val maxCount = (launchCounts.values.maxOrNull() ?: 0).coerceAtLeast(1)

    val allAppsWithUsage = allCatalogApps.map { app ->
        val count = launchCounts[app.packageName] ?: 0
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
            val count = launchCounts[app.packageName] ?: 0
            app.copy(
                launchCount = count,
                usageRatio = (count.toFloat() / maxCount.toFloat()).coerceIn(0.15f, 1f)
            )
        }
    }

    val currentTelemetry = TelemetryState(
        latitude = liveLocation?.latitude,
        longitude = liveLocation?.longitude,
        isGpsEnabled = isGpsEnabled && hasLocationPermission,
        tempCelsius = system.tempCelsius,
        cpuPercent = system.cpuPercent,
        ramPercent = system.ramPercent
    )

    val weatherForHome = storedWeather.copy(
        isOffline = !effectiveOnline,
        hoursSinceUpdate = storedWeather.updatedAtEpochMs?.let {
            ((System.currentTimeMillis() - it) / 3_600_000L).toInt().coerceAtLeast(1)
        }
    )

    fun requestGpsOrLocationPermission() {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (!isGpsEnabled) {
            LocationTracker.openLocationSettings(context)
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
                telemetry = currentTelemetry,
                weather = weatherForHome,
                gridApps = gridCandidateApps,
                dockApps = dockApps,
                settings = storedSettings,
                activeTrip = activeTrip,
                pendingUpdate = if (!bannerDismissed) availableUpdate else null,
                onUpdateBannerDownload = { currentRoute = OctoRoute.UPDATE },
                onUpdateBannerLater = { bannerDismissed = true },
                onTripClick = { currentRoute = OctoRoute.MAP },
                onGpsHeaderClick = { requestGpsOrLocationPermission() },
                onOpenSettingsClick = { currentRoute = OctoRoute.SETTINGS },
                onOpenOverclockClick = { currentRoute = OctoRoute.OVERCLOCK },
                onClockClick = { currentRoute = OctoRoute.CLOCK },
                onWeatherClick = { currentRoute = OctoRoute.WEATHER },
                nowPlaying = nowPlaying,
                onMediaPlayPause = { MediaSessionRepository.togglePlayPause() },
                onMediaNext = { MediaSessionRepository.next() },
                onMediaPrevious = { MediaSessionRepository.previous() },
                onAppClick = { entry ->
                    when {
                        entry.packageName == InstalledAppsHelper.ALL_APPS_PACKAGE -> {
                            currentRoute = OctoRoute.ALL_APPS
                        }

                        entry.iconType == OctoIconType.MAPA || entry.label == "MAPA" -> {
                            scope.launch { prefsRepo.incrementLaunchCount(entry.packageName) }
                            currentRoute = OctoRoute.MAP
                        }

                        entry.packageName == InstalledAppsHelper.SETTINGS_PACKAGE -> {
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
                        entry.packageName == InstalledAppsHelper.SETTINGS_PACKAGE -> currentRoute = OctoRoute.SETTINGS
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
                userLocation = liveLocation,
                telemetry = currentTelemetry,
                weather = storedWeather,
                isOnline = effectiveOnline,
                isGpsEnabled = isGpsEnabled,
                hasLocationPermission = hasLocationPermission,
                onRequestGpsOrPermission = { requestGpsOrLocationPermission() },
                onStartRealTrip = { computedTrip ->
                    scope.launch { prefsRepo.saveActiveTrip(computedTrip) }
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
                onAccentChange = { accent ->
                    scope.launch { prefsRepo.setAccent(accent) }
                },
                onToggleAnimatedBackground = { enabled ->
                    scope.launch { prefsRepo.setAnimatedBackgroundEnabled(enabled) }
                },
                onToggleAutoUpdate = { enabled ->
                    scope.launch { prefsRepo.setAutoUpdateEnabled(enabled) }
                },
                onToggleOfflineMode = { enabled ->
                    scope.launch { prefsRepo.setOfflineModeEnabled(enabled) }
                },
                onOpenOverclockClick = { currentRoute = OctoRoute.OVERCLOCK },
                onOpenMapClick = { currentRoute = OctoRoute.MAP },
                mediaAccessGranted = mediaAccess,
                onOpenMediaAccess = {
                    context.startActivity(
                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
                onOpenLockScreen = {
                    context.startActivity(Intent(context, LockScreenActivity::class.java))
                },
                onCheckUpdateClick = {
                    scope.launch {
                        when (val result = GithubUpdateManager.checkLatestRelease()) {
                            is UpdateCheckResult.Available -> {
                                availableUpdate = result.info
                                bannerDismissed = false
                                downloadProgressPercent = 0
                                currentRoute = OctoRoute.UPDATE
                            }
                            UpdateCheckResult.UpToDate -> {
                                Toast.makeText(context, "Você já está na versão mais recente.", Toast.LENGTH_SHORT).show()
                            }
                            UpdateCheckResult.Failed -> {
                                Toast.makeText(context, "Não foi possível verificar atualizações.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                onBackClick = { currentRoute = OctoRoute.HOME },
                modifier = modifier
            )
        }

        OctoRoute.OVERCLOCK -> {
            OverclockScreen(
                selectedMode = storedSettings.overclockMode,
                cpuPercent = system.cpuPercent,
                ramPercent = system.ramPercent,
                temperatureCelsius = system.tempCelsius,
                onSelectMode = { mode ->
                    scope.launch { prefsRepo.setOverclockMode(mode) }
                },
                onBackClick = { currentRoute = OctoRoute.SETTINGS },
                modifier = modifier
            )
        }

        OctoRoute.CLOCK -> {
            ClockScreen(
                now = now,
                onBackClick = { currentRoute = OctoRoute.HOME }
            )
        }

        OctoRoute.WEATHER -> {
            WeatherDetailScreen(
                weather = weatherForHome,
                latitude = liveLocation?.latitude,
                longitude = liveLocation?.longitude,
                isOnline = effectiveOnline,
                onBackClick = { currentRoute = OctoRoute.HOME }
            )
        }

        OctoRoute.UPDATE -> {
            val info = availableUpdate ?: UpdateReleaseInfo()
            UpdateScreen(
                releaseInfo = info,
                downloadProgressPercent = downloadProgressPercent,
                onInstallClick = {
                    downloadProgressPercent = 0
                    scope.launch {
                        val ok = GithubUpdateManager.downloadAndInstallApk(
                            context = context,
                            releaseInfo = info,
                            onProgressPercent = { pct -> downloadProgressPercent = pct }
                        )
                        if (!ok) {
                            Toast.makeText(context, "Não foi possível instalar a atualização.", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onLaterClick = {
                    bannerDismissed = true
                    currentRoute = OctoRoute.HOME
                },
                modifier = modifier
            )
        }
    }
}
