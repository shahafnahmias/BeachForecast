package io.beachforecast.domain.usecases

import io.beachforecast.domain.models.Beach
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Find the closest beach to given GPS coordinates
 * Uses Haversine formula for accurate distance calculation
 * Pure Kotlin - no Android dependencies
 */
class FindClosestBeachUseCase {

    /**
     * Find the closest beach to the user's location
     * @param userLatitude User's GPS latitude
     * @param userLongitude User's GPS longitude
     * @param beaches List of available beaches
     * @return The closest beach
     * @throws IllegalArgumentException if beaches list is empty
     */
    fun execute(
        userLatitude: Double,
        userLongitude: Double,
        beaches: List<Beach>
    ): Beach {
        require(beaches.isNotEmpty()) { "Beach list cannot be empty" }

        return beaches.minBy { beach ->
            haversineDistance(
                userLatitude, userLongitude,
                beach.latitude, beach.longitude
            )
        }
    }

    /**
     * Calculate distance between two points on Earth in kilometers
     * Uses Haversine formula for great-circle distance
     *
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    fun haversineDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }
}
