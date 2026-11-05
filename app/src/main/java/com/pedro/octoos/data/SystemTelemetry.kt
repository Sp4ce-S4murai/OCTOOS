package com.pedro.octoos.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.io.File

data class SystemSnapshot(
    val tempCelsius: Int?,
    val cpuPercent: Int?,
    val ramPercent: Int?
)

/** Leituras reais do aparelho. Temperatura vem do sensor da bateria (único acessível a apps). */
object SystemTelemetry {
    private var lastCpuTotal = 0L
    private var lastCpuIdle = 0L

    fun read(context: Context): SystemSnapshot = SystemSnapshot(
        tempCelsius = readBatteryTemp(context),
        cpuPercent = readCpuPercent(),
        ramPercent = readRamPercent(context)
    )

    private fun readBatteryTemp(context: Context): Int? {
        val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val tenths = battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            ?: return null
        if (tenths == Int.MIN_VALUE) return null
        return tenths / 10
    }

    /** Uso de CPU calculado entre duas leituras de /proc/stat. Null se o sistema bloquear o arquivo. */
    private fun readCpuPercent(): Int? {
        return try {
            val line = File("/proc/stat").useLines { lines -> lines.firstOrNull() } ?: return null
            val values = line.trim().split(Regex("\\s+")).drop(1).map { it.toLong() }
            if (values.size < 4) return null
            val idle = values[3] + values.getOrElse(4) { 0L }
            val total = values.sum()
            val firstSample = lastCpuTotal == 0L
            val dTotal = total - lastCpuTotal
            val dIdle = idle - lastCpuIdle
            lastCpuTotal = total
            lastCpuIdle = idle
            if (firstSample || dTotal <= 0L) {
                null
            } else {
                (100L * (dTotal - dIdle) / dTotal).toInt().coerceIn(0, 100)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun readRamPercent(context: Context): Int? {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return null
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        if (info.totalMem <= 0L) return null
        return (100L * (info.totalMem - info.availMem) / info.totalMem).toInt().coerceIn(0, 100)
    }
}
