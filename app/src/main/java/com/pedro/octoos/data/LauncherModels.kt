package com.pedro.octoos.data

import com.pedro.octoos.ui.components.OctoIconType

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

