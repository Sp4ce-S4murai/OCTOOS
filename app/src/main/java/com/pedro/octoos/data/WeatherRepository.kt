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

data class HourlyPoint(
    val hour: Int,
    val temperatureCelsius: Int,
    val weatherCode: Int,
    val precipitationPercent: Int
)

/** Previsão horária de hoje (fuso automático) via Open-Meteo. Retorna null se falhar. */
fun WeatherRepository.fetchToday(latitude: Double, longitude: Double): List<HourlyPoint>? {
    val endpoint = String.format(
        Locale.US,
        "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f" +
            "&hourly=temperature_2m,weather_code,precipitation_probability" +
            "&forecast_days=1&timezone=auto",
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
            val hourly = JSONObject(payload).getJSONObject("hourly")
            val times = hourly.getJSONArray("time")
            val temps = hourly.getJSONArray("temperature_2m")
            val codes = hourly.getJSONArray("weather_code")
            val rain = hourly.optJSONArray("precipitation_probability")
            (0 until times.length()).map { i ->
                HourlyPoint(
                    hour = times.getString(i).substring(11, 13).toInt(),
                    temperatureCelsius = Math.round(temps.getDouble(i)).toInt(),
                    weatherCode = codes.getInt(i),
                    precipitationPercent = rain?.optInt(i, 0) ?: 0
                )
            }
        }
    } catch (e: Exception) {
        null
    } finally {
        connection.disconnect()
    }
}
