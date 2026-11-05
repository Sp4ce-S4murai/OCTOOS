package com.pedro.octoos.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ResolvedDestination(
    val formattedName: String,
    val latLng: LatLng
)

object LocationTracker {

    /** Dark cyber-brutal Google Maps JSON style matching #0A0A0A / #161616 / #222D21 */
    const val CYBER_BRUTAL_MAP_STYLE_JSON = """
        [
          {"elementType":"geometry","stylers":[{"color":"#0e130d"}]},
          {"elementType":"labels.text.fill","stylers":[{"color":"#8e8e9c"}]},
          {"elementType":"labels.text.stroke","stylers":[{"color":"#0a0a0a"}]},
          {"featureType":"road","elementType":"geometry","stylers":[{"color":"#222d21"}]},
          {"featureType":"road.arterial","elementType":"geometry","stylers":[{"color":"#2b3a29"}]},
          {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#374b34"}]},
          {"featureType":"water","elementType":"geometry","stylers":[{"color":"#070907"}]},
          {"featureType":"poi","stylers":[{"visibility":"off"}]},
          {"featureType":"transit","stylers":[{"visibility":"off"}]}
        ]
    """

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isGpsProviderEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return runCatching {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }.getOrDefault(false)
    }

    @SuppressLint("MissingPermission")
    fun readCurrentLocation(context: Context): LatLng? {
        if (!hasLocationPermission(context) || !isGpsProviderEnabled(context)) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        var bestLocation: Location? = null
        for (provider in providers) {
            val loc = runCatching { lm.getLastKnownLocation(provider) }.getOrNull() ?: continue
            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                bestLocation = loc
            }
        }
        return bestLocation?.let { LatLng(it.latitude, it.longitude) }
    }

    fun openLocationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    /**
     * Resolves any address string into real LatLng coordinates using Android's Geocoder.
     */
    @Suppress("DEPRECATION")
    suspend fun geocodeAddress(
        context: Context,
        query: String
    ): ResolvedDestination? = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext null

        val geocoded = runCatching {
            if (!Geocoder.isPresent()) return@runCatching null
            val geocoder = Geocoder(context, Locale("pt", "BR"))
            val results = geocoder.getFromLocationName(trimmed, 1)
            val first = results?.firstOrNull() ?: return@runCatching null
            val street = first.thoroughfare ?: first.featureName ?: trimmed
            ResolvedDestination(
                formattedName = street,
                latLng = LatLng(first.latitude, first.longitude)
            )
        }.getOrNull()

        geocoded
    }

    /**
     * Builds an ActiveTripState with real distance, duration, and ETA computed between origin and destination.
     */
    fun buildRealTripState(
        origin: LatLng,
        destination: ResolvedDestination,
        travelMode: TravelMode
    ): ActiveTripState {
        val results = FloatArray(1)
        Location.distanceBetween(
            origin.latitude,
            origin.longitude,
            destination.latLng.latitude,
            destination.latLng.longitude,
            results
        )
        val straightMeters = results[0].coerceAtLeast(150f)
        // Urban road factor ~1.28x straight line
        val routeMeters = straightMeters * 1.28f
        val routeKm = routeMeters / 1000f

        val speedMetersPerMin = if (travelMode == TravelMode.A_PE) 78f else 480f
        val durationMin = (routeMeters / speedMetersPerMin).toInt().coerceAtLeast(2)
        val firstTurnMeters = (straightMeters * 0.18f).toInt().coerceIn(50, 900)

        val eta = LocalTime.now().plusMinutes(durationMin.toLong())
            .format(DateTimeFormatter.ofPattern("HH:mm"))

        val distanceKmFormatted = String.format(Locale("pt", "BR"), "%.1f km", routeKm)
        val durationFormatted = if (durationMin >= 60) {
            "${durationMin / 60}h ${durationMin % 60}m"
        } else {
            "$durationMin min"
        }

        return ActiveTripState(
            isActive = true,
            destinationAddress = destination.formattedName,
            originLat = origin.latitude,
            originLng = origin.longitude,
            destinationLat = destination.latLng.latitude,
            destinationLng = destination.latLng.longitude,
            arrivalTimeLabel = eta,
            nextManeuverDistanceLabel = "$firstTurnMeters M",
            remainingDurationLabel = durationFormatted,
            maneuverInstruction = "Vire à direita · ${destination.formattedName}",
            remainingDistanceKmLabel = distanceKmFormatted,
            travelMode = travelMode
        )
    }
}
