package com.pedro.octoos.data

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class WeatherReading(
    val temperatureCelsius: Int,
    val humidityPercent: Int,
    val windKmh: Int
)

/** Clima atual via Open-Meteo (sem chave de API). Retorna null se falhar. */
object WeatherRepository {
    fun fetchCurrent(latitude: Double, longitude: Double): WeatherReading? {
        val endpoint = String.format(
            Locale.US,
            "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f" +
                "&current=temperature_2m,relative_humidity_2m,wind_speed_10m&wind_speed_unit=kmh",
            latitude,
            longitude
        )
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            connectTimeout = 6000
            readTimeout = 6000
        }
        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                null
            } else {
                val payload = connection.inputStream.bufferedReader().use { it.readText() }
                val current = JSONObject(payload).getJSONObject("current")
                WeatherReading(
                    temperatureCelsius = Math.round(current.getDouble("temperature_2m")).toInt(),
                    humidityPercent = current.getInt("relative_humidity_2m"),
                    windKmh = Math.round(current.getDouble("wind_speed_10m")).toInt()
                )
            }
        } catch (e: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }
}
