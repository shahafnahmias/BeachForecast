package io.beachforecast.domain.usecases

import io.beachforecast.domain.models.HourlyWaveForecast
import io.beachforecast.domain.models.WaveCategory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

/**
 * Groups today's remaining forecast into 3-hour periods
 * Shows periods until 1 hour after sunset, max 5 periods (15 hours)
 * Pure Kotlin - no Android dependencies
 */
class GroupTodayByPeriodsUseCase {

    data class PeriodData(
        val startHour: Int,
        val endHour: Int,
        val timeLabel: String,  // "Now", "15:00", "18:00", etc
        val avgWaveHeight: Double,
        val waveCategory: WaveCategory,
        val avgTemp: Double,
        val avgUvIndex: Double,
        val avgWindSpeed: Double,
        val avgWindDirection: Int,
        val avgSwellHeight: Double,
        val avgSwellDirection: Int,
        val avgSwellPeriod: Double,
        val avgWavePeriod: Double,
        val avgCloudCover: Int
    )

    fun execute(todayRemaining: List<HourlyWaveForecast>): List<PeriodData> {
        if (todayRemaining.isEmpty()) return emptyList()

        val periods = mutableListOf<PeriodData>()
        val now = LocalDateTime.now()
        val currentHour = now.hour

        // Calculate sunset (approximate - use 18:00 + 1 hour = 19:00 as cutoff for now)
        // TODO: Use proper sunset calculation based on latitude
        val sunsetHour = 18
        val cutoffHour = min(sunsetHour + 1, 24)

        // Generate 3-hour periods from current hour until cutoff, max 5 periods
        var startHour = currentHour
        var periodCount = 0

        while (startHour < cutoffHour && periodCount < 5) {
            val endHour = min(startHour + 3, cutoffHour)

            // Get data for this period
            val periodData = todayRemaining.filter { hourly ->
                val hour = extractHour(hourly.time)
                hour in startHour until endHour
            }

            if (periodData.isEmpty()) {
                startHour = endHour
                continue
            }

            // Calculate averages
            val avgWaveHeight = periodData.map { it.waveHeightMeters }.average()
            val avgTemp = periodData.map { it.temperatureCelsius }.average()
            val avgUvIndex = periodData.map { it.uvIndex }.average()
            val avgWindSpeed = periodData.map { it.windSpeedKmh }.average()
            val avgWindDirection = periodData.map { it.windDirectionDegrees }.average().toInt()
            val avgSwellHeight = periodData.map { it.swellHeightMeters }.average()
            val avgSwellDirection = periodData.map { it.swellDirectionDegrees }.average().toInt()
            val avgSwellPeriod = periodData.map { it.swellPeriodSeconds }.average()
            val avgWavePeriod = periodData.map { it.wavePeriodSeconds }.average()
            // CloudCoverLevel is an enum - calculate average from midpoint of each level's range
            val avgCloudCover = periodData.map {
                (it.cloudCover.minCover + it.cloudCover.maxCover) / 2
            }.average().toInt()

            // Format time label (first period is "Now", others show start time)
            val timeLabel = if (periodCount == 0) {
                "Now"
            } else {
                String.format("%02d:00", startHour % 24)
            }

            periods.add(
                PeriodData(
                    startHour = startHour,
                    endHour = endHour,
                    timeLabel = timeLabel,
                    avgWaveHeight = avgWaveHeight,
                    waveCategory = WaveCategory.fromHeight(avgWaveHeight),
                    avgTemp = avgTemp,
                    avgUvIndex = avgUvIndex,
                    avgWindSpeed = avgWindSpeed,
                    avgWindDirection = avgWindDirection,
                    avgSwellHeight = avgSwellHeight,
                    avgSwellDirection = avgSwellDirection,
                    avgSwellPeriod = avgSwellPeriod,
                    avgWavePeriod = avgWavePeriod,
                    avgCloudCover = avgCloudCover
                )
            )

            startHour = endHour
            periodCount++
        }

        return periods
    }

    private fun extractHour(timeString: String): Int {
        return try {
            val dateTime = LocalDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME)
            dateTime.hour
        } catch (e: Exception) {
            0
        }
    }
}
