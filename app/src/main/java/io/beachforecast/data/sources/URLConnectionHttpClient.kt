package io.beachforecast.data.sources

import io.beachforecast.config.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL

/**
 * HttpURLConnection implementation of HttpClient
 */
class URLConnectionHttpClient : HttpClient {

    override suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val urlObj = URL(url)
        val connection = urlObj.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = ApiConfig.CONNECTION_TIMEOUT_MS
            connection.readTimeout = ApiConfig.READ_TIMEOUT_MS

            // Disable caching to ensure fresh data on every request
            // This prevents widget from showing stale cached data
            connection.useCaches = false
            connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
            connection.setRequestProperty("Pragma", "no-cache")
            connection.setRequestProperty("Expires", "0")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                Timber.e("HTTP %d error for URL: %s", responseCode, url)
                throw Exception("HTTP error code: $responseCode - $errorBody")
            }
        } finally {
            connection.disconnect()
        }
    }
}
