package io.beachforecast

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.FirebaseApp
import io.beachforecast.logging.CrashlyticsTree
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.Locale

/**
 * Application class to handle app-wide locale configuration
 */
class SeaLevelApplication : Application() {

    companion object {
        private const val DEFAULT_LANGUAGE = "en"
        private val Context.dataStore by preferencesDataStore(name = "user_preferences")
        private val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    override fun attachBaseContext(base: Context) {
        // Read language preference directly from DataStore to avoid UserPreferences dependency
        val languageCode = try {
            runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                base.dataStore.data.first()[APP_LANGUAGE] ?: DEFAULT_LANGUAGE
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to read language preference, using default")
            DEFAULT_LANGUAGE
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
