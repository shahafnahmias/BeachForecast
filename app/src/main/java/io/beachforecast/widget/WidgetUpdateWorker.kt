package io.beachforecast.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.beachforecast.MainActivity
import io.beachforecast.R
import io.beachforecast.data.location.LocationProvider
import io.beachforecast.data.preferences.UserPreferences
import io.beachforecast.data.repository.BeachRepository
import io.beachforecast.data.repository.WeatherRepository
import io.beachforecast.domain.models.Activity
import io.beachforecast.domain.models.CurrentConditions
import io.beachforecast.domain.models.UnitSystem
import io.beachforecast.domain.models.WeatherMetric
import io.beachforecast.domain.models.WeatherResult
import io.beachforecast.domain.models.WidgetThemeColors
import io.beachforecast.domain.usecases.FindClosestBeachUseCase
import io.beachforecast.domain.usecases.GetSelectedBeachUseCase
import io.beachforecast.domain.usecases.GetWidgetDataUseCase
import io.beachforecast.presentation.binders.LargeWidgetBinder
import io.beachforecast.presentation.binders.MediumWidgetBinder
import io.beachforecast.presentation.binders.SmallWidgetBinder
import timber.log.Timber
import java.util.Locale

class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "widget_update"
    }

    override suspend fun doWork(): Result {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        val smallIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, SmallWidgetProvider::class.java)
        )
        val mediumIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, MediumWidgetProvider::class.java)
        )
        val largeIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, LargeWidgetProvider::class.java)
        )

        if (smallIds.isEmpty() && mediumIds.isEmpty() && largeIds.isEmpty()) {
            return Result.success()
        }

        val userPreferences = UserPreferences(context)
        val languageCode = userPreferences.getAppLanguage()
        val localizedContext = updateContextLocale(context, languageCode)
        val unitSystem = userPreferences.getUnitSystem()
        val appTheme = userPreferences.getAppTheme()
        val selectedMetrics = userPreferences.getSelectedMetrics()
        val selectedSports = userPreferences.getSelectedSports()
        val themeColors = WidgetThemeColors.fromTheme(appTheme)
        val layoutDirection = if (languageCode == "he") {
            android.view.View.LAYOUT_DIRECTION_RTL
        } else {
            android.view.View.LAYOUT_DIRECTION_LTR
        }

        // Initialize dependencies
        val locationProvider = LocationProvider(context)
        val beachRepository = BeachRepository(context)
        val weatherRepository = WeatherRepository()
        val getSelectedBeachUseCase = GetSelectedBeachUseCase(
            beachRepository = beachRepository,
            locationProvider = locationProvider,
            userPreferences = userPreferences,
            findClosestBeachUseCase = FindClosestBeachUseCase()
        )
        val getWidgetDataUseCase = GetWidgetDataUseCase(
            getSelectedBeachUseCase = getSelectedBeachUseCase,
            weatherRepository = weatherRepository,
            languageCode = languageCode
        )

        try {
            val result = getWidgetDataUseCase.execute()

            if (result is WeatherResult.Error) {
                val errorMessage = localizedContext.getString(result.error.getUserMessageRes())
                showErrorOnAllWidgets(
                    appWidgetManager, localizedContext, themeColors, layoutDirection,
                    errorMessage, smallIds, mediumIds, largeIds
                )
                return Result.retry()
            }

            val widgetData = result.getOrNull() ?: return Result.retry()
            val cityName = widgetData.cityName
            val weatherData = widgetData.weatherData
            val conditions = weatherData.currentConditions

            // Generate summary only if large widgets exist
            val summaryText = if (largeIds.isNotEmpty()) {
                GenerateTodaySummaryUseCase.execute(
                    localizedContext, conditions, weatherData.todayRemaining,
                    selectedMetrics, selectedSports
                )
            } else ""

            // Update small widgets
            for (widgetId in smallIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_small)
                views.setInt(R.id.widget_small_root, "setLayoutDirection", layoutDirection)
                val binder = SmallWidgetBinder(localizedContext, views)
                binder.applyTheme(themeColors)
                binder.bind(cityName, conditions, unitSystem, selectedSports)
                setupClickHandlers(context, views, R.id.widget_small_root, widgetId)
                appWidgetManager.updateAppWidget(widgetId, views)
            }

            // Update medium widgets
            for (widgetId in mediumIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_medium)
                views.setInt(R.id.widget_medium_root, "setLayoutDirection", layoutDirection)
                val binder = MediumWidgetBinder(localizedContext, views)
                binder.applyTheme(themeColors)
                binder.bind(cityName, conditions, unitSystem, selectedSports, themeColors)
                setupClickHandlers(context, views, R.id.widget_medium_root, widgetId)
                appWidgetManager.updateAppWidget(widgetId, views)
            }

            // Update large widgets
            for (widgetId in largeIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_large)
                views.setInt(R.id.widget_large_root, "setLayoutDirection", layoutDirection)
                val binder = LargeWidgetBinder(localizedContext, views)
                binder.applyTheme(themeColors)
                binder.bind(cityName, conditions, unitSystem, selectedSports, summaryText, themeColors)
                setupClickHandlers(context, views, R.id.widget_large_root, widgetId)
                appWidgetManager.updateAppWidget(widgetId, views)
            }

            return Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Widget update failed")
            showErrorOnAllWidgets(
                appWidgetManager, localizedContext, themeColors, layoutDirection,
                localizedContext.getString(R.string.error_unknown),
                smallIds, mediumIds, largeIds
            )
            return Result.retry()
        }
    }

    private fun showErrorOnAllWidgets(
        appWidgetManager: AppWidgetManager,
        localizedContext: Context,
        themeColors: WidgetThemeColors,
        layoutDirection: Int,
        errorMessage: String,
        smallIds: IntArray,
        mediumIds: IntArray,
        largeIds: IntArray
    ) {
        for (widgetId in smallIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_small)
            views.setInt(R.id.widget_small_root, "setLayoutDirection", layoutDirection)
            val binder = SmallWidgetBinder(localizedContext, views)
            binder.applyTheme(themeColors)
            binder.showError(errorMessage)
            setupClickHandlers(context, views, R.id.widget_small_root, widgetId)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
        for (widgetId in mediumIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_medium)
            views.setInt(R.id.widget_medium_root, "setLayoutDirection", layoutDirection)
            val binder = MediumWidgetBinder(localizedContext, views)
            binder.applyTheme(themeColors)
            binder.showError(errorMessage)
            setupClickHandlers(context, views, R.id.widget_medium_root, widgetId)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
        for (widgetId in largeIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_large)
            views.setInt(R.id.widget_large_root, "setLayoutDirection", layoutDirection)
            val binder = LargeWidgetBinder(localizedContext, views)
            binder.applyTheme(themeColors)
            binder.showError(errorMessage)
            setupClickHandlers(context, views, R.id.widget_large_root, widgetId)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    private fun setupClickHandlers(context: Context, views: RemoteViews, rootViewId: Int, appWidgetId: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(rootViewId, pendingIntent)
    }

    private fun updateContextLocale(context: Context, languageCode: String): Context {
        val locale = if (languageCode == "he") Locale("he") else Locale("en")
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

        return context.createConfigurationContext(configuration)
    }
}
