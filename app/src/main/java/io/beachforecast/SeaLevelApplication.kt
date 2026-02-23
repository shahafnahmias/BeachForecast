package io.beachforecast

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import io.beachforecast.data.preferences.UserPreferences
import io.beachforecast.logging.CrashlyticsTree
import timber.log.Timber
import java.util.Locale

/**
 * Application class to handle app-wide locale configuration
 */
class SeaLevelApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Apply analytics preference
        val analyticsEnabled = try {
            UserPreferences(this).isAnalyticsEnabledSync()
        } catch (e: Exception) {
            true // Default to enabled if preference read fails
        }
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(analyticsEnabled)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    override fun attachBaseContext(base: Context) {
        // Use SharedPreferences sync layer to avoid blocking on DataStore during app startup
        val languageCode = try {
            val userPreferences = UserPreferences(base)
            userPreferences.getAppLanguageSync()
        } catch (e: Exception) {
            // Fallback to default language if UserPreferences fails to initialize (e.g., in test environment)
            "en"
        }

        val localizedContext = createConfigurationContext(base, languageCode)
        super.attachBaseContext(localizedContext)
    }

    private fun createConfigurationContext(context: Context, languageCode: String): Context {
        val locale = if (languageCode == "he") {
            Locale("he")
        } else {
            Locale("en")
        }
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }
}
