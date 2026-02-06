package io.beachforecast.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import io.beachforecast.config.ApiConfig
import io.beachforecast.data.sources.AndroidLocationDataSource
import io.beachforecast.data.sources.LocationDataSource
import io.beachforecast.domain.models.WeatherError
import io.beachforecast.domain.models.WeatherResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.util.Locale

/**
 * Location provider with caching and error handling
 * Uses LocationDataSource interface for testability
 */
class LocationProvider(
    private val context: Context,
    private val locationDataSource: LocationDataSource = AndroidLocationDataSource(context)
) {

    private val locationCache = LocationCache(context)

    /**
     * Get location with detailed error handling
     * Falls back to cached location if fresh location unavailable
     */
    suspend fun getLocation(): WeatherResult<Location> {
        return try {
            // Check permissions
            val hasPermission = checkLocationPermission()
            if (!hasPermission) {
                // Try cached location as fallback
                return getCachedLocationFallback() ?: WeatherResult.Error(WeatherError.LocationPermissionDenied)
            }

            // Check if location services are enabled
            if (!isLocationEnabled()) {
                // Try cached location as fallback
                return getCachedLocationFallback() ?: WeatherResult.Error(WeatherError.LocationDisabled)
            }

            // Get location with timeout
            val location = withTimeout(ApiConfig.LOCATION_TIMEOUT_MS) {
                fetchLocation()
            }

            if (location == null) {
                // Try cached location as fallback
                return getCachedLocationFallback() ?: WeatherResult.Error(WeatherError.LocationUnavailable)
            }

            // Cache the successful location
            locationCache.saveLocation(location)
            WeatherResult.Success(location)

        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            // Try cached location as fallback
            getCachedLocationFallback() ?: WeatherResult.Error(WeatherError.LocationTimeout)

        } catch (e: SecurityException) {
            WeatherResult.Error(WeatherError.LocationPermissionDenied)

        } catch (e: Exception) {
            // Try cached location as fallback
            getCachedLocationFallback() ?: WeatherResult.Error(WeatherError.Unknown(e))
        }
    }

    /**
     * Get cached location as fallback
     */
    private fun getCachedLocationFallback(): WeatherResult<Location>? {
        val cachedLocation = locationCache.getCachedLocation()
        return if (cachedLocation != null) {
            WeatherResult.Success(cachedLocation)
        } else {
            null
        }
    }

    /**
     * Get city name with error handling
     */
    suspend fun getCityName(latitude: Double, longitude: Double): WeatherResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                withTimeout(ApiConfig.GEOCODING_TIMEOUT_MS) {
                    val geocoder = Geocoder(context, Locale.getDefault())

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        var cityName = "Unknown Location"
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            if (addresses.isNotEmpty()) {
                                val address = addresses[0]
                                cityName = address.locality
                                    ?: address.subLocality
                                    ?: address.subAdminArea
                                    ?: address.adminArea
                                    ?: address.featureName
                                    ?: "Unknown Location"
                            }
                        }
                        kotlinx.coroutines.delay(500)
                        WeatherResult.Success(cityName)
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val result = address.locality
                                ?: address.subLocality
                                ?: address.subAdminArea
                                ?: address.adminArea
                                ?: address.featureName
                                ?: "Unknown Location"
                            WeatherResult.Success(result)
                        } else {
                            WeatherResult.Success("Unknown Location")
                        }
                    }
                }
            } catch (e: Exception) {
                WeatherResult.Success("Unknown Location") // Geocoding failure is not critical
            }
        }
    }

    /**
     * Check if location permission is granted
     */
    private fun checkLocationPermission(): Boolean {
        val hasFineLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return hasFineLocation || hasCoarseLocation
    }

    /**
     * Check if location services are enabled
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    /**
     * Fetch location from LocationDataSource
     * Prioritizes last known location for faster widget updates
     */
    private suspend fun fetchLocation(): Location? {
        return try {
            // Try last known location first (faster for widgets, always available)
            val lastLocation = locationDataSource.getLastKnownLocation()
            if (lastLocation != null) {
                return lastLocation
            }

            // If no last known location, try current (slower, requires GPS fix)
            locationDataSource.getCurrentLocation()
        } catch (e: Exception) {
            Timber.e(e, "fetchLocation failed")
            null
        }
    }
}
