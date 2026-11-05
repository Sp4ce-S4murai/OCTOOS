package com.pedro.octoos.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "octoos_prefs")

class LauncherPreferencesRepository(private val context: Context) {

    private val weatherTempKey = intPreferencesKey("weather_temp_c")
    private val weatherHumidityKey = intPreferencesKey("weather_humidity")
    private val weatherWindKey = intPreferencesKey("weather_wind_kmh")
    private val weatherUpdatedEpochKey = longPreferencesKey("weather_updated_epoch")

    private val orbitModeKey = booleanPreferencesKey("settings_orbit_mode")
    private val showUsageBarsKey = booleanPreferencesKey("settings_show_usage_bars")
    private val hapticFeedbackKey = booleanPreferencesKey("settings_haptic_feedback")
    private val clockScalePercentKey = intPreferencesKey("settings_clock_scale_percent")
    private val gridAppsLimitKey = intPreferencesKey("settings_grid_apps_limit")
    private val overclockModeKey = stringPreferencesKey("settings_overclock_mode")

    fun launchCountsFlow(packageNames: List<String>): Flow<Map<String, Int>> {
        return context.dataStore.data.map { prefs ->
            packageNames.associateWith { pkg ->
                prefs[launchCountKey(pkg)] ?: defaultSeedCount(pkg)
            }
        }
    }

    suspend fun incrementLaunchCount(packageName: String) {
        context.dataStore.edit { prefs ->
            val key = launchCountKey(packageName)
            val current = prefs[key] ?: defaultSeedCount(packageName)
            prefs[key] = current + 1
        }
    }

    val weatherFlow: Flow<WeatherState> = context.dataStore.data.map { prefs ->
        val temp = prefs[weatherTempKey] ?: 24
        val humidity = prefs[weatherHumidityKey] ?: 61
        val wind = prefs[weatherWindKey] ?: 12
        val updatedAt = prefs[weatherUpdatedEpochKey] ?: System.currentTimeMillis()
        val elapsedHours = ((System.currentTimeMillis() - updatedAt) / 3_600_000L)
            .toInt()
            .coerceAtLeast(1)
        WeatherState(
            temperatureCelsius = temp,
            humidityPercent = humidity,
            windKmh = wind,
            isOffline = false,
            hoursSinceUpdate = elapsedHours
        )
    }

    val settingsFlow: Flow<SettingsState> = context.dataStore.data.map { prefs ->
        val modeName = prefs[overclockModeKey] ?: OverclockMode.EQUILIBRIO.name
        val mode = runCatching { OverclockMode.valueOf(modeName) }
            .getOrDefault(OverclockMode.EQUILIBRIO)
        SettingsState(
            orbitModeEnabled = prefs[orbitModeKey] ?: true,
            showUsageBars = prefs[showUsageBarsKey] ?: true,
            hapticFeedback = prefs[hapticFeedbackKey] ?: true,
            clockScalePercent = prefs[clockScalePercentKey] ?: 100,
            gridAppsLimit = prefs[gridAppsLimitKey] ?: 4,
            overclockMode = mode
        )
    }

    suspend fun setOrbitModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[orbitModeKey] = enabled }
    }

    suspend fun setShowUsageBars(enabled: Boolean) {
        context.dataStore.edit { it[showUsageBarsKey] = enabled }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[hapticFeedbackKey] = enabled }
    }

    suspend fun setClockScalePercent(percent: Int) {
        context.dataStore.edit { it[clockScalePercentKey] = percent.coerceIn(80, 120) }
    }

    suspend fun setGridAppsLimit(limit: Int) {
        context.dataStore.edit { it[gridAppsLimitKey] = limit.coerceIn(4, 8) }
    }

    suspend fun setOverclockMode(mode: OverclockMode) {
        context.dataStore.edit { it[overclockModeKey] = mode.name }
    }

    private fun launchCountKey(packageName: String) =
        intPreferencesKey("launch_count_$packageName")

    private fun defaultSeedCount(packageName: String): Int = when {
        packageName.contains("claude", ignoreCase = true) -> 42
        packageName.contains("maps", ignoreCase = true) -> 28
        packageName.contains("mms", ignoreCase = true) ||
            packageName.contains("messaging", ignoreCase = true) -> 35
        packageName.contains("git", ignoreCase = true) -> 24
        else -> 12
    }
}
