package io.beachforecast.data.location

import android.location.Location
import io.beachforecast.data.sources.CacheStorage
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocationCacheTest {

    private lateinit var mockCacheStorage: CacheStorage
    private lateinit var locationCache: LocationCache

    @Before
    fun setup() {
        mockCacheStorage = mock()
        locationCache = LocationCache(mockCacheStorage)
    }

    private fun createLocation(latitude: Double, longitude: Double): Location {
        return Location("test").apply {
            this.latitude = latitude
            this.longitude = longitude
        }
    }

    @Test
    fun `saveLocation stores coordinates and timestamp`() {
        // Given
        val testLocation = createLocation(32.0853, 34.7818)

        // When
        locationCache.saveLocation(testLocation)

        // Then
        verify(mockCacheStorage).saveFloat("latitude", 32.0853f)
        verify(mockCacheStorage).saveFloat("longitude", 34.7818f)
        verify(mockCacheStorage).saveLong(eq("timestamp"), any())
    }

    @Test
    fun `getCachedLocation returns location when cache is valid`() {
        // Given
        val currentTime = System.currentTimeMillis()
        whenever(mockCacheStorage.contains("latitude")).thenReturn(true)
        whenever(mockCacheStorage.contains("longitude")).thenReturn(true)
        whenever(mockCacheStorage.getFloat("latitude", 0f)).thenReturn(32.0853f)
        whenever(mockCacheStorage.getFloat("longitude", 0f)).thenReturn(34.7818f)
        whenever(mockCacheStorage.getLong("timestamp", 0L)).thenReturn(currentTime)

        // When
        val result = locationCache.getCachedLocation()

        // Then
        assertNotNull(result)
        assertEquals(32.0853, result!!.latitude, 0.0001)
        assertEquals(34.7818, result.longitude, 0.0001)
        assertEquals("cache", result.provider)
    }

    @Test
    fun `getCachedLocation returns null when no cache keys exist`() {
        // Given
        whenever(mockCacheStorage.contains("latitude")).thenReturn(false)
        whenever(mockCacheStorage.contains("longitude")).thenReturn(false)

        // When
        val result = locationCache.getCachedLocation()

        // Then
        assertNull(result)
    }

    @Test
    fun `getCachedLocation returns location at zero coordinates when cached`() {
        // Given - location at (0,0) is valid (Gulf of Guinea)
        val currentTime = System.currentTimeMillis()
        whenever(mockCacheStorage.contains("latitude")).thenReturn(true)
        whenever(mockCacheStorage.contains("longitude")).thenReturn(true)
        whenever(mockCacheStorage.getFloat("latitude", 0f)).thenReturn(0f)
        whenever(mockCacheStorage.getFloat("longitude", 0f)).thenReturn(0f)
        whenever(mockCacheStorage.getLong("timestamp", 0L)).thenReturn(currentTime)

        // When
        val result = locationCache.getCachedLocation()

        // Then
        assertNotNull(result)
        assertEquals(0.0, result!!.latitude, 0.0001)
        assertEquals(0.0, result.longitude, 0.0001)
    }

    @Test
    fun `getCachedLocation returns null when cache is too old`() {
        // Given
        val oldTime = System.currentTimeMillis() - (8 * 24 * 60 * 60 * 1000L) // 8 days ago (cache expires after 7 days)
        whenever(mockCacheStorage.contains("latitude")).thenReturn(true)
        whenever(mockCacheStorage.contains("longitude")).thenReturn(true)
        whenever(mockCacheStorage.getFloat("latitude", 0f)).thenReturn(32.0853f)
        whenever(mockCacheStorage.getFloat("longitude", 0f)).thenReturn(34.7818f)
        whenever(mockCacheStorage.getLong("timestamp", 0L)).thenReturn(oldTime)

        // When
        val result = locationCache.getCachedLocation()

        // Then
        assertNull(result)
    }

    @Test
    fun `hasCachedLocation returns true when valid cache exists`() {
        // Given
        val currentTime = System.currentTimeMillis()
        whenever(mockCacheStorage.contains("latitude")).thenReturn(true)
        whenever(mockCacheStorage.contains("longitude")).thenReturn(true)
        whenever(mockCacheStorage.getFloat("latitude", 0f)).thenReturn(32.0853f)
        whenever(mockCacheStorage.getFloat("longitude", 0f)).thenReturn(34.7818f)
        whenever(mockCacheStorage.getLong("timestamp", 0L)).thenReturn(currentTime)

        // When
        val result = locationCache.hasCachedLocation()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasCachedLocation returns false when no valid cache exists`() {
        // Given
        whenever(mockCacheStorage.contains("latitude")).thenReturn(false)
        whenever(mockCacheStorage.contains("longitude")).thenReturn(false)

        // When
        val result = locationCache.hasCachedLocation()

        // Then
        assertFalse(result)
    }

    @Test
    fun `clear calls cacheStorage clear`() {
        // When
        locationCache.clear()

        // Then
        verify(mockCacheStorage).clear()
    }
}
