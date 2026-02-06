package io.beachforecast.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.beachforecast.domain.models.AppTheme
import io.beachforecast.domain.models.Activity
import io.beachforecast.domain.models.BeachSelection
import io.beachforecast.domain.models.UnitSystem
import io.beachforecast.domain.models.WeatherMetric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * User preferences using DataStore for persistence
 * Handles all app-wide settings
 */
class UserPreferences(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
        private val USE_METRIC = booleanPreferencesKey("use_metric")
        private val APP_THEME = stringPreferencesKey("app_theme")
        private val SELECTED_METRICS = stringPreferencesKey("selected_metrics")
        private val APP_LANGUAGE = stringPreferencesKey("app_language")
        private val SELECTED_BEACH = stringPreferencesKey("selected_beach")
        private val SELECTED_SPORTS = stringPreferencesKey("selected_sports")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // Defaults
        private const val DEFAULT_USE_METRIC = true
        private val DEFAULT_THEME = AppTheme.SYSTEM
        private const val DEFAULT_LANGUAGE = "en" // English

        // SharedPreferences for synchronous reads (language, onboarding)
        private const val SYNC_PREFS_NAME = "sync_prefs"
        private const val SYNC_LANGUAGE = "sync_language"
        private const val SYNC_ONBOARDING = "sync_onboarding"
    }

    private val syncPrefs by lazy {
        context.getSharedPreferences(SYNC_PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Read language synchronously from SharedPreferences (no coroutine needed).
     * Used in attachBaseContext where runBlocking must be avoided.
     */
    fun getAppLanguageSync(): String {
        return syncPrefs.getString(SYNC_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    /**
     * Read onboarding completed synchronously from SharedPreferences.
     */
    fun isOnboardingCompletedSync(): Boolean {
        return syncPrefs.getBoolean(SYNC_ONBOARDING, false)
    }

    /**
     * Flow of unit system preference
     */
    val unitSystemFlow: Flow<UnitSystem> = context.dataStore.data.map { preferences ->
        val useMetric = preferences[USE_METRIC] ?: DEFAULT_USE_METRIC
        UnitSystem.fromBoolean(useMetric)
    }

    /**
     * Get current unit system synchronously (for widget)
     * This reads from DataStore which may block, use in coroutine or background thread
     */
    suspend fun getUnitSystem(): UnitSystem {
        val preferences = context.dataStore.data.first()
        val useMetric = preferences[USE_METRIC] ?: DEFAULT_USE_METRIC
        return UnitSystem.fromBoolean(useMetric)
    }

    /**
     * Set unit system preference
     */
    suspend fun setUnitSystem(unitSystem: UnitSystem) {
        context.dataStore.edit { preferences ->
            preferences[USE_METRIC] = unitSystem.toBoolean()
        }
    }

    /**
     * Set use metric preference
     */
    suspend fun setUseMetric(useMetric: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_METRIC] = useMetric
        }
    }

    /**
     * Flow of app theme preference
     */
    val appThemeFlow: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        val themeString = preferences[APP_THEME]
        AppTheme.fromString(themeString)
    }

    /**
     * Get current theme synchronously (for widget)
     */
    suspend fun getAppTheme(): AppTheme {
        val preferences = context.dataStore.data.first()
        val themeString = preferences[APP_THEME]
        return AppTheme.fromString(themeString)
    }

    /**
     * Set app theme preference
     */
    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = theme.name
        }
    }

    /**
     * Generic enum set parser from comma-separated string.
     */
    private inline fun <reified T : Enum<T>> parseEnumSet(
        raw: String?,
        defaults: () -> Set<T>
    ): Set<T> {
        if (raw.isNullOrEmpty()) return defaults()
        return raw.split(",")
            .mapNotNull { name ->
                try {
                    enumValueOf<T>(name)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
            .toSet()
    }

    /**
     * Flow of selected weather metrics
     */
    val selectedMetricsFlow: Flow<Set<WeatherMetric>> = context.dataStore.data.map { preferences ->
        parseEnumSet(preferences[SELECTED_METRICS]) { WeatherMetric.getDefaults() }
    }

    /**
     * Get selected metrics synchronously (for widget)
     */
    suspend fun getSelectedMetrics(): Set<WeatherMetric> {
        val preferences = context.dataStore.data.first()
        return parseEnumSet(preferences[SELECTED_METRICS]) { WeatherMetric.getDefaults() }
    }

    /**
     * Set selected weather metrics
     */
    suspend fun setSelectedMetrics(metrics: Set<WeatherMetric>) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_METRICS] = metrics.joinToString(",") { it.name }
        }
    }

    /**
     * Flow of selected sports
     */
    val selectedSportsFlow: Flow<Set<Activity>> = context.dataStore.data.map { preferences ->
        parseEnumSet(preferences[SELECTED_SPORTS]) { Activity.getDefaults() }
    }

    /**
     * Get selected sports synchronously (for widget)
     */
    suspend fun getSelectedSports(): Set<Activity> {
        val preferences = context.dataStore.data.first()
        return parseEnumSet(preferences[SELECTED_SPORTS]) { Activity.getDefaults() }
    }

    /**
     * Set selected sports
     */
    suspend fun setSelectedSports(sports: Set<Activity>) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_SPORTS] = sports.joinToString(",") { it.name }
        }
    }

    /**
     * Flow of onboarding completed preference
     */
    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    /**
     * Get onboarding completed synchronously
     */
    suspend fun isOnboardingCompleted(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[ONBOARDING_COMPLETED] ?: false
    }

    /**
     * Set onboarding completed
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        syncPrefs.edit().putBoolean(SYNC_ONBOARDING, completed).apply()
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    /**
     * Flow of app language preference
     */
    val appLanguageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_LANGUAGE] ?: DEFAULT_LANGUAGE
    }

    /**
     * Get current language synchronously (for widget)
     */
    suspend fun getAppLanguage(): String {
        val preferences = context.dataStore.data.first()
        return preferences[APP_LANGUAGE] ?: DEFAULT_LANGUAGE
    }

    /**
     * Set app language preference
     * @param languageCode ISO 639-1 language code (e.g., "en", "he")
     */
    suspend fun setAppLanguage(languageCode: String) {
        syncPrefs.edit().putString(SYNC_LANGUAGE, languageCode).apply()
        context.dataStore.edit { preferences ->
            preferences[APP_LANGUAGE] = languageCode
        }
    }

    /**
     * Flow of beach selection preference
     */
    val beachSelectionFlow: Flow<BeachSelection> = context.dataStore.data.map { preferences ->
        BeachSelection.fromString(preferences[SELECTED_BEACH])
    }

    /**
     * Get current beach selection synchronously (for widget)
     */
    suspend fun getBeachSelection(): BeachSelection {
        val preferences = context.dataStore.data.first()
        return BeachSelection.fromString(preferences[SELECTED_BEACH])
    }

    /**
     * Set beach selection preference
     */
    suspend fun setBeachSelection(selection: BeachSelection) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_BEACH] = BeachSelection.toStorageString(selection)
        }
    }
}
