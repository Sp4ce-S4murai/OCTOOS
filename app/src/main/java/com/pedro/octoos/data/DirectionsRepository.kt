package com.pedro.octoos.data

import com.pedro.octoos.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class RouteResult(
    val distanceMeters: Int,
    val durationSeconds: Int,
    val points: List<Pair<Double, Double>>,
    val firstStepMeters: Int,
    val firstStepInstruction: String
)

/** Rota real pela Directions API do Google (usa MAPS_API_KEY). Sem cache, conforme os termos do Maps. */
object DirectionsRepository {
    fun fetchRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        travelMode: TravelMode
    ): RouteResult? {
        if (BuildConfig.MAPS_API_KEY.isBlank()) return null
        val origin = String.format(Locale.US, "%.6f,%.6f", originLat, originLng)
        val destination = String.format(Locale.US, "%.6f,%.6f", destLat, destLng)
        val endpoint = "https://maps.googleapis.com/maps/api/directions/json" +
            "?origin=" + URLEncoder.encode(origin, "UTF-8") +
            "&destination=" + URLEncoder.encode(destination, "UTF-8") +
            "&mode=" + travelMode.directionsMode +
            "&language=pt-BR&key=" + URLEncoder.encode(BuildConfig.MAPS_API_KEY, "UTF-8")
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8000
            readTimeout = 8000
        }
        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            if (json.optString("status") != "OK") return null
            val leg = json.getJSONArray("routes").getJSONObject(0)
                .getJSONArray("legs").getJSONObject(0)
            val steps = leg.getJSONArray("steps")
            val firstStep = steps.getJSONObject(0)
            val points = decodePolyline(
                json.getJSONArray("routes").getJSONObject(0)
                    .getJSONObject("overview_polyline").getString("points")
            )
            RouteResult(
                distanceMeters = leg.getJSONObject("distance").getInt("value"),
                durationSeconds = leg.getJSONObject("duration").getInt("value"),
                points = points,
                firstStepMeters = firstStep.getJSONObject("distance").getInt("value"),
                firstStepInstruction = firstStep.optString("html_instructions")
                    .replace(Regex("<[^>]*>"), "")
                    .trim()
            )
        } catch (e: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    /** Decodifica o formato encoded polyline do Google. */
    fun decodePolyline(encoded: String): List<Pair<Double, Double>> {
        val result = mutableListOf<Pair<Double, Double>>()
        var index = 0
        var lat = 0
        var lng = 0
        while (index < encoded.length) {
            var shift = 0
            var value = 0
            var b: Int
            do {
                b = encoded[index++].code - 63
                value = value or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            lat += if ((value and 1) != 0) (value shr 1).inv() else value shr 1

            shift = 0
            value = 0
            do {
                b = encoded[index++].code - 63
                value = value or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            lng += if ((value and 1) != 0) (value shr 1).inv() else value shr 1

            result.add(Pair(lat / 1e5, lng / 1e5))
        }
        return result
    }
}
