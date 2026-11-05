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
    private val tripDistanceKey = stringPreferencesKey("trip_distance")
    private val tripDurationKey = stringPreferencesKey("trip_duration")
    private val tripNextStepKey = stringPreferencesKey("trip_next_step")
    private val tripInstructionKey = stringPreferencesKey("trip_instruction")
    private val tripArrivalKey = stringPreferencesKey("trip_arrival")
    private val tripPolylineKey = stringPreferencesKey("trip_polyline")

    fun launchCountsFlow(packageNames: List<String>): Flow<Map<String, Int>> {
        return context.dataStore.data.map { prefs ->
            packageNames.associateWith { pkg -> prefs[launchCountKey(pkg)] ?: 0 }
        }
    }

    suspend fun incrementLaunchCount(packageName: String) {
        context.dataStore.edit { prefs ->
            val key = launchCountKey(packageName)
            prefs[key] = (prefs[key] ?: 0) + 1
        }
    }

    suspend fun saveWeather(reading: WeatherReading) {
        context.dataStore.edit {
            it[weatherTempKey] = reading.temperatureCelsius
            it[weatherHumidityKey] = reading.humidityPercent
            it[weatherWindKey] = reading.windKmh
            it[weatherUpdatedEpochKey] = System.currentTimeMillis()
        }
    }

    /** Último clima obtido. Sem leitura salva, os campos ficam nulos. */
    val weatherFlow: Flow<WeatherState> = context.dataStore.data.map { prefs ->
        val updatedAt = prefs[weatherUpdatedEpochKey]
        WeatherState(
            temperatureCelsius = prefs[weatherTempKey],
            humidityPercent = prefs[weatherHumidityKey],
            windKmh = prefs[weatherWindKey],
            isOffline = false,
            updatedAtEpochMs = updatedAt
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
        val modeName = prefs[tripModeKey] ?: TravelMode.CARRO.name
        val mode = runCatching { TravelMode.valueOf(modeName) }.getOrDefault(TravelMode.CARRO)
        ActiveTripState(
            isActive = prefs[tripActiveKey] ?: false,
            destinationAddress = prefs[tripDestinationKey] ?: "",
            originLat = prefs[tripOriginLatKey] ?: 0.0,
            originLng = prefs[tripOriginLngKey] ?: 0.0,
            destinationLat = prefs[tripDestLatKey] ?: 0.0,
            destinationLng = prefs[tripDestLngKey] ?: 0.0,
            arrivalTimeLabel = prefs[tripArrivalKey] ?: "--:--",
            nextManeuverDistanceLabel = prefs[tripNextStepKey] ?: "--",
            remainingDurationLabel = prefs[tripDurationKey] ?: "--",
            maneuverInstruction = prefs[tripInstructionKey] ?: "",
            remainingDistanceKmLabel = prefs[tripDistanceKey] ?: "--",
            travelMode = mode,
            routePolyline = decodeStoredPolyline(prefs[tripPolylineKey])
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
            it[tripDistanceKey] = trip.remainingDistanceKmLabel
            it[tripDurationKey] = trip.remainingDurationLabel
            it[tripNextStepKey] = trip.nextManeuverDistanceLabel
            it[tripInstructionKey] = trip.maneuverInstruction
            it[tripArrivalKey] = trip.arrivalTimeLabel
            it[tripPolylineKey] = encodeStoredPolyline(trip.routePolyline)
        }
    }

    suspend fun stopTrip() {
        context.dataStore.edit {
            it[tripActiveKey] = false
        }
    }

    private fun launchCountKey(packageName: String) =
        intPreferencesKey("launch_count_$packageName")

    private fun encodeStoredPolyline(points: List<Pair<Double, Double>>): String =
        points.joinToString(";") { "${it.first},${it.second}" }

    private fun decodeStoredPolyline(raw: String?): List<Pair<Double, Double>> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(";").mapNotNull { pair ->
            val parts = pair.split(",")
            val lat = parts.getOrNull(0)?.toDoubleOrNull()
            val lng = parts.getOrNull(1)?.toDoubleOrNull()
            if (lat != null && lng != null) Pair(lat, lng) else null
        }
    }
}
