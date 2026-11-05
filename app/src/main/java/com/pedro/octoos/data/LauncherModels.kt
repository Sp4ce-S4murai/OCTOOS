package com.pedro.octoos.data

import com.pedro.octoos.ui.components.OctoIconType

enum class OverclockMode(
    val label: String,
    val shortChipLabel: String,
    val animationsEnabled: Boolean,
    val animationDurationMs: Int,
    val preloadedAppsCount: Int,
    val clockUpdateIntervalMs: Long,
    val baseCpuPercent: Int,
    val baseRamPercent: Int,
    val baseTempCelsius: Int
) {
    ECO(
        label = "ECO",
        shortChipLabel = "ECO",
        animationsEnabled = false,
        animationDurationMs = 0,
        preloadedAppsCount = 4,
        clockUpdateIntervalMs = 5000L,
        baseCpuPercent = 28,
        baseRamPercent = 40,
        baseTempCelsius = 43
    ),
    EQUILIBRIO(
        label = "EQUILÍBRIO",
        shortChipLabel = "EQUIL.",
        animationsEnabled = true,
        animationDurationMs = 900,
        preloadedAppsCount = 8,
        clockUpdateIntervalMs = 1000L,
        baseCpuPercent = 50,
        baseRamPercent = 50,
        baseTempCelsius = 52
    ),
    OC(
        label = "OC",
        shortChipLabel = "OC",
        animationsEnabled = true,
        animationDurationMs = 350,
        preloadedAppsCount = 16,
        clockUpdateIntervalMs = 250L,
        baseCpuPercent = 90,
        baseRamPercent = 50,
        baseTempCelsius = 78
    )
}

enum class TravelMode(val label: String, val mapsModeCode: String) {
    CARRO("CARRO", "d"),
    A_PE("A PÉ", "w")
}

data class ActiveTripState(
    val isActive: Boolean = false,
    val destinationAddress: String = "Av. Independência",
    val originLat: Double = -23.5505,
    val originLng: Double = -46.6333,
    val destinationLat: Double = -23.5681,
    val destinationLng: Double = -46.6492,
    val arrivalTimeLabel: String = "14:21",
    val nextManeuverDistanceLabel: String = "300 M",
    val remainingDurationLabel: String = "12 min",
    val maneuverInstruction: String = "Vire à direita · Av. Independência",
    val remainingDistanceKmLabel: String = "4,2 km",
    val travelMode: TravelMode = TravelMode.CARRO
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
    val overclockMode: OverclockMode = OverclockMode.EQUILIBRIO,
    val simulatedTempOverride: Int? = null
) {
    val effectiveCpuTemp: Int
        get() = simulatedTempOverride ?: overclockMode.baseTempCelsius
}

data class AppEntry(
    val label: String,
    val packageName: String,
    val monogram: String,
    val iconType: OctoIconType? = null,
    val launchCount: Int = 0,
    val usageRatio: Float = 0.5f,
    val firstInstallTime: Long = 0L,
    val recencyLabel: String = "HOJE"
)

data class WeatherState(
    val temperatureCelsius: Int = 24,
    val humidityPercent: Int = 61,
    val windKmh: Int = 12,
    val isOffline: Boolean = false,
    val hoursSinceUpdate: Int = 2
)

data class TelemetryState(
    val latitude: Double = 36.1749,
    val longitude: Double = -86.6676,
    val hasGpsFix: Boolean = true,
    val isGpsEnabled: Boolean = true,
    val cpuTempCelsius: Int = 52
)
