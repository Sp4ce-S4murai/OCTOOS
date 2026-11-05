package com.pedro.octoos.data

import com.pedro.octoos.ui.components.OctoIconType

enum class OverclockMode(
    val label: String,
    val shortChipLabel: String,
    val animationsEnabled: Boolean,
    val animationDurationMs: Int,
    val preloadedAppsCount: Int,
    val clockUpdateIntervalMs: Long
) {
    ECO(
        label = "ECO",
        shortChipLabel = "ECO",
        animationsEnabled = false,
        animationDurationMs = 0,
        preloadedAppsCount = 4,
        clockUpdateIntervalMs = 5000L
    ),
    EQUILIBRIO(
        label = "EQUILÍBRIO",
        shortChipLabel = "EQUIL.",
        animationsEnabled = true,
        animationDurationMs = 900,
        preloadedAppsCount = 8,
        clockUpdateIntervalMs = 1000L
    ),
    OC(
        label = "OC",
        shortChipLabel = "OC",
        animationsEnabled = true,
        animationDurationMs = 350,
        preloadedAppsCount = 16,
        clockUpdateIntervalMs = 250L
    )
}

enum class TravelMode(val label: String, val mapsModeCode: String, val directionsMode: String) {
    CARRO("CARRO", "d", "driving"),
    A_PE("A PÉ", "w", "walking")
}

data class ActiveTripState(
    val isActive: Boolean = false,
    val destinationAddress: String = "",
    val originLat: Double = 0.0,
    val originLng: Double = 0.0,
    val destinationLat: Double = 0.0,
    val destinationLng: Double = 0.0,
    val arrivalTimeLabel: String = "--:--",
    val nextManeuverDistanceLabel: String = "--",
    val remainingDurationLabel: String = "--",
    val maneuverInstruction: String = "",
    val remainingDistanceKmLabel: String = "--",
    val travelMode: TravelMode = TravelMode.CARRO,
    val routePolyline: List<Pair<Double, Double>> = emptyList()
)

data class SettingsState(
    val orbitModeEnabled: Boolean = true,
    val showUsageBars: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animationsEnabled: Boolean = true,
    val autoUpdateEnabled: Boolean = true,
    val offlineModeEnabled: Boolean = false,
    val performanceSliderFraction: Float = 0.54f,
    val clockScalePercent: Int = 100,
    val gridAppsLimit: Int = 4,
    val overclockMode: OverclockMode = OverclockMode.EQUILIBRIO
)

data class AppEntry(
    val label: String,
    val packageName: String,
    val monogram: String,
    val iconType: OctoIconType? = null,
    val launchCount: Int = 0,
    val usageRatio: Float = 0.15f,
    val firstInstallTime: Long = 0L,
    val recencyLabel: String = ""
)

/** Clima real vindo da API; campos nulos enquanto não houver leitura. */
data class WeatherState(
    val temperatureCelsius: Int? = null,
    val humidityPercent: Int? = null,
    val windKmh: Int? = null,
    val isOffline: Boolean = false,
    val updatedAtEpochMs: Long? = null,
    val hoursSinceUpdate: Int? = null
)

/** Telemetria real do aparelho. Nulos quando o dado não está disponível. */
data class TelemetryState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isGpsEnabled: Boolean = false,
    val tempCelsius: Int? = null,
    val cpuPercent: Int? = null,
    val ramPercent: Int? = null
)
