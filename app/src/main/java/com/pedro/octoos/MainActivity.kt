package com.pedro.octoos

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.pedro.octoos.data.InstalledAppsHelper
import com.pedro.octoos.data.LauncherPreferencesRepository
import com.pedro.octoos.data.TelemetryState
import com.pedro.octoos.data.WeatherState
import com.pedro.octoos.ui.components.formatPortugueseShortDate
import com.pedro.octoos.ui.home.HomeScreen
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoOSTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

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

    val baseGridApps = remember { InstalledAppsHelper.defaultHomeGridApps() }
    val dockApps = remember { InstalledAppsHelper.defaultDockApps() }
    val packageNames = remember { baseGridApps.map { it.packageName } }

    val launchCounts by prefsRepo.launchCountsFlow(packageNames)
        .collectAsState(initial = emptyMap())
    val storedWeather by prefsRepo.weatherFlow
        .collectAsState(initial = WeatherState())

    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var isConnected by remember { mutableStateOf(InstalledAppsHelper.isNetworkConnected(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            isConnected = InstalledAppsHelper.isNetworkConnected(context)
            delay(1000L)
        }
    }

    val maxCount = (launchCounts.values.maxOrNull() ?: 50).coerceAtLeast(1)
    val gridAppsWithUsage = baseGridApps.map { app ->
        val count = launchCounts[app.packageName] ?: app.launchCount
        app.copy(
            launchCount = count,
            usageRatio = (count.toFloat() / maxCount.toFloat()).coerceIn(0.20f, 1f)
        )
    }

    HomeScreen(
        hours24 = now.hour,
        minutes = now.minute,
        seconds = now.second,
        dateLabel = formatPortugueseShortDate(now),
        telemetry = TelemetryState(),
        weather = storedWeather.copy(isOffline = !isConnected),
        gridApps = gridAppsWithUsage,
        dockApps = dockApps,
        onAppClick = { entry ->
            scope.launch {
                prefsRepo.incrementLaunchCount(entry.packageName)
            }
            InstalledAppsHelper.launchAppOrFallback(context, entry)
        },
        modifier = modifier
    )
}
