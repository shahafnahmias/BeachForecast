package io.beachforecast.data.sources

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Android implementation of LocationDataSource
 * Uses FusedLocationProviderClient for location access
 */
class AndroidLocationDataSource(private val context: Context) : LocationDataSource {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    override suspend fun getCurrentLocation(): Location? {
        return try {
            val cancellationTokenSource = CancellationTokenSource()

            @Suppress("MissingPermission") // Permission checked by caller
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get current location")
            null
        }
    }

    override suspend fun getLastKnownLocation(): Location? {
        return try {
            @Suppress("MissingPermission") // Permission checked by caller
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get last known location")
            null
        }
    }
}
