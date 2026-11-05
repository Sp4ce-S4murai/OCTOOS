package com.pedro.octoos.data

import com.pedro.octoos.ui.components.OctoIconType

enum class OverclockMode(
    val label: String,
    val animationsEnabled: Boolean,
    val animationDurationMs: Int,
    val preloadedAppsCount: Int,
    val clockUpdateIntervalMs: Long,
    val baseCpuPercent: Int,
    val baseRamPercent: Int,
    val baseTempCelsius: Int
) {
    ECO(
        label = "Eco",
        animationsEnabled = false,
        animationDurationMs = 0,
        preloadedAppsCount = 4,
        clockUpdateIntervalMs = 5000L,
        baseCpuPercent = 18,
        baseRamPercent = 34,
        baseTempCelsius = 41
    ),
    EQUILIBRIO(
        label = "Equilíbrio",
        animationsEnabled = true,
        animationDurationMs = 900,
        preloadedAppsCount = 8,
        clockUpdateIntervalMs = 1000L,
        baseCpuPercent = 42,
        baseRamPercent = 54,
        baseTempCelsius = 52
    ),
    OC(
        label = "OC",
        animationsEnabled = true,
        animationDurationMs = 350,
        preloadedAppsCount = 16,
        clockUpdateIntervalMs = 250L,
        baseCpuPercent = 86,
        baseRamPercent = 78,
        baseTempCelsius = 64
    )
}

data class SettingsState(
    val orbitModeEnabled: Boolean = true,
    val showUsageBars: Boolean = true,
    val hapticFeedback: Boolean = true,
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
    val firstInstallTime: Long = 0L
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
    val cpuTempCelsius: Int = 52
)
