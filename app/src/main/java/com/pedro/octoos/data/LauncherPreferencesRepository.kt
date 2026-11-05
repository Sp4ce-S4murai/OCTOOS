package com.pedro.octoos.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
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
    private val animationsKey = booleanPreferencesKey("settings_animations_enabled")
    private val autoUpdateKey = booleanPreferencesKey("settings_auto_update_enabled")
    private val offlineModeKey = booleanPreferencesKey("settings_offline_mode_enabled")
    private val performanceFractionKey = floatPreferencesKey("settings_performance_fraction")
    private val clockScalePercentKey = intPreferencesKey("settings_clock_scale_percent")
    private val gridAppsLimitKey = intPreferencesKey("settings_grid_apps_limit")
    private val overclockModeKey = stringPreferencesKey("settings_overclock_mode")

    private val tripActiveKey = booleanPreferencesKey("trip_is_active")
    private val tripDestinationKey = stringPreferencesKey("trip_destination")
    private val tripModeKey = stringPreferencesKey("trip_travel_mode")
    private val tripOriginLatKey = doublePreferencesKey("trip_origin_lat")
    private val tripOriginLngKey = doublePreferencesKey("trip_origin_lng")
    private val tripDestLatKey = doublePreferencesKey("trip_dest_lat")
    private val tripDestLngKey = doublePreferencesKey("trip_dest_lng")
    private val tripDistanceKmKey = stringPreferencesKey("trip_distance_km")
    private val tripDurationKey = stringPreferencesKey("trip_duration")
    private val tripNextStepKey = stringPreferencesKey("trip_next_step")
    private val tripArrivalKey = stringPreferencesKey("trip_arrival")

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
        val defaultSlider = when (mode) {
            OverclockMode.ECO -> 0.25f
            OverclockMode.EQUILIBRIO -> 0.54f
            OverclockMode.OC -> 0.92f
        }
        SettingsState(
            orbitModeEnabled = prefs[orbitModeKey] ?: true,
            showUsageBars = prefs[showUsageBarsKey] ?: true,
            hapticFeedback = prefs[hapticFeedbackKey] ?: true,
            animationsEnabled = prefs[animationsKey] ?: mode.animationsEnabled,
            autoUpdateEnabled = prefs[autoUpdateKey] ?: true,
            offlineModeEnabled = prefs[offlineModeKey] ?: false,
            performanceSliderFraction = prefs[performanceFractionKey] ?: defaultSlider,
            clockScalePercent = prefs[clockScalePercentKey] ?: 100,
            gridAppsLimit = prefs[gridAppsLimitKey] ?: 4,
            overclockMode = mode
        )
    }

    val activeTripFlow: Flow<ActiveTripState> = context.dataStore.data.map { prefs ->
        val isActive = prefs[tripActiveKey] ?: false
        val destination = prefs[tripDestinationKey] ?: "Av. Independência"
        val modeName = prefs[tripModeKey] ?: TravelMode.CARRO.name
        val mode = runCatching { TravelMode.valueOf(modeName) }.getOrDefault(TravelMode.CARRO)
        val isWalking = mode == TravelMode.A_PE

        ActiveTripState(
            isActive = isActive,
            destinationAddress = destination,
            originLat = prefs[tripOriginLatKey] ?: -23.5505,
            originLng = prefs[tripOriginLngKey] ?: -46.6333,
            destinationLat = prefs[tripDestLatKey] ?: -23.5681,
            destinationLng = prefs[tripDestLngKey] ?: -46.6492,
            arrivalTimeLabel = prefs[tripArrivalKey] ?: if (isWalking) "14:48" else "14:21",
            nextManeuverDistanceLabel = prefs[tripNextStepKey] ?: if (isWalking) "120 M" else "300 M",
            remainingDurationLabel = prefs[tripDurationKey] ?: if (isWalking) "38 min" else "12 min",
            maneuverInstruction = "Vire à direita · $destination",
            remainingDistanceKmLabel = prefs[tripDistanceKmKey] ?: if (isWalking) "2,8 km" else "4,2 km",
            travelMode = mode
        )
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[animationsKey] = enabled }
    }

    suspend fun setAutoUpdateEnabled(enabled: Boolean) {
        context.dataStore.edit { it[autoUpdateKey] = enabled }
    }

    suspend fun setOfflineModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[offlineModeKey] = enabled }
    }

    suspend fun setOverclockMode(mode: OverclockMode) {
        val fraction = when (mode) {
            OverclockMode.ECO -> 0.25f
            OverclockMode.EQUILIBRIO -> 0.54f
            OverclockMode.OC -> 0.92f
        }
        context.dataStore.edit {
            it[overclockModeKey] = mode.name
            it[performanceFractionKey] = fraction
            it[animationsKey] = mode.animationsEnabled
        }
    }

    suspend fun setPerformanceSliderFraction(fraction: Float) {
        val clamped = fraction.coerceIn(0.1f, 1f)
        val mode = when {
            clamped < 0.38f -> OverclockMode.ECO
            clamped < 0.75f -> OverclockMode.EQUILIBRIO
            else -> OverclockMode.OC
        }
        context.dataStore.edit {
            it[performanceFractionKey] = clamped
            it[overclockModeKey] = mode.name
            it[animationsKey] = mode.animationsEnabled
        }
    }

    suspend fun saveActiveTrip(trip: ActiveTripState) {
        context.dataStore.edit {
            it[tripActiveKey] = trip.isActive
            it[tripDestinationKey] = trip.destinationAddress
            it[tripModeKey] = trip.travelMode.name
            it[tripOriginLatKey] = trip.originLat
            it[tripOriginLngKey] = trip.originLng
            it[tripDestLatKey] = trip.destinationLat
            it[tripDestLngKey] = trip.destinationLng
            it[tripDistanceKmKey] = trip.remainingDistanceKmLabel
            it[tripDurationKey] = trip.remainingDurationLabel
            it[tripNextStepKey] = trip.nextManeuverDistanceLabel
            it[tripArrivalKey] = trip.arrivalTimeLabel
        }
    }

    suspend fun stopTrip() {
        context.dataStore.edit {
            it[tripActiveKey] = false
        }
    }

    private fun launchCountKey(packageName: String) =
        intPreferencesKey("launch_count_$packageName")

    private fun defaultSeedCount(packageName: String): Int = when {
        packageName.contains("whatsapp", ignoreCase = true) -> 42
        packageName.contains("camera", ignoreCase = true) -> 18
        packageName.contains("git", ignoreCase = true) -> 9
        packageName.contains("maps", ignoreCase = true) -> 4
        packageName.contains("claude", ignoreCase = true) -> 3
        else -> 2
    }
}
