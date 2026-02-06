package io.beachforecast.integration

import android.location.Location
import io.beachforecast.data.location.LocationCache
import io.beachforecast.fakes.FakeCacheStorage
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Integration test for LocationCache using FakeCacheStorage
 * Tests the full flow of saving and retrieving cached locations
 */
@RunWith(RobolectricTestRunner::class)
class LocationCacheIntegrationTest {

    private lateinit var cacheStorage: FakeCacheStorage
    private lateinit var locationCache: LocationCache

    @Before
    fun setup() {
        cacheStorage = FakeCacheStorage()
        locationCache = LocationCache(cacheStorage)
    }

    private fun createLocation(latitude: Double, longitude: Double): Location {
        return Location("test").apply {
            this.latitude = latitude
            this.longitude = longitude
        }
    }

    @Test
    fun `full cycle - save location and retrieve it`() {
        // Given
        val location = createLocation(32.0853, 34.7818)

        // When
        locationCache.saveLocation(location)
        val retrieved = locationCache.getCachedLocation()

        // Then
        assertNotNull(retrieved)
        assertEquals(32.0853, retrieved!!.latitude, 0.0001)
        assertEquals(34.7818, retrieved.longitude, 0.0001)
        assertEquals("cache", retrieved.provider)
    }

    @Test
    fun `saved location is valid for 24 hours`() {
        // Given
        val location = createLocation(32.0853, 34.7818)

        // When - save location
        locationCache.saveLocation(location)

        // Simulate time passing (but less than 24 hours)
        // We can't actually wait, but the fake storage preserves the timestamp

        // Then - should still be valid
        assertTrue(locationCache.hasCachedLocation())
        assertNotNull(locationCache.getCachedLocation())
    }

    @Test
    fun `clear removes all cached data`() {
        // Given
        val location = createLocation(32.0853, 34.7818)
        locationCache.saveLocation(location)
        assertTrue(locationCache.hasCachedLocation())

        // When
        locationCache.clear()

        // Then
        assertFalse(locationCache.hasCachedLocation())
        assertNull(locationCache.getCachedLocation())
    }

    @Test
    fun `multiple save operations - last one wins`() {
        // Given
        val location1 = createLocation(32.0, 34.0)
        val location2 = createLocation(31.0, 35.0)

        // When
        locationCache.saveLocation(location1)
        locationCache.saveLocation(location2)
        val retrieved = locationCache.getCachedLocation()

        // Then - should get the second location
        assertNotNull(retrieved)
        assertEquals(31.0, retrieved!!.latitude, 0.0001)
        assertEquals(35.0, retrieved.longitude, 0.0001)
    }

    @Test
    fun `zero coordinates are valid when keys exist in cache`() {
        // Given - manually set zero coordinates in storage (e.g. Gulf of Guinea)
        cacheStorage.saveFloat("latitude", 0f)
        cacheStorage.saveFloat("longitude", 0f)
        cacheStorage.saveLong("timestamp", System.currentTimeMillis())

        // When
        val result = locationCache.getCachedLocation()

        // Then - (0,0) is a valid location when explicitly cached
        assertNotNull(result)
        assertEquals(0.0, result!!.latitude, 0.0001)
        assertEquals(0.0, result.longitude, 0.0001)
    }

    @Test
    fun `empty cache returns null`() {
        // Given - nothing has been saved

        // When
        val result = locationCache.getCachedLocation()

        // Then
        assertNull(result)
        assertFalse(locationCache.hasCachedLocation())
    }

    @Test
    fun `can verify underlying storage is used`() {
        // Given
        val location = createLocation(32.0853, 34.7818)

        // When
        locationCache.saveLocation(location)

        // Then - verify data is actually in the fake storage
        assertEquals(32.0853f, cacheStorage.getFloat("latitude", 0f), 0.0001f)
        assertEquals(34.7818f, cacheStorage.getFloat("longitude", 0f), 0.0001f)
        assertTrue(cacheStorage.getLong("timestamp", 0L) > 0)
    }
}
