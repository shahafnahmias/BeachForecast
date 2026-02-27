package io.beachforecast.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.beachforecast.MainActivity
import io.beachforecast.widget.components.ConditionBadge
import io.beachforecast.widget.components.PrimarySportHero
import io.beachforecast.widget.components.WidgetErrorContent
import io.beachforecast.widget.components.WidgetHeader
import io.beachforecast.widget.components.WidgetLoadingContent
import io.beachforecast.widget.theme.BeachForecastWidgetTheme

class TodayForecastWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BeachForecastWidgetTheme {
                TodayForecastContent()
            }
        }
    }
}

class TodayForecastWidgetReceiver : BeachForecastWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayForecastWidget()
}

@Composable
private fun TodayForecastContent() {
    val context = LocalContext.current
    val prefs = currentState<Preferences>()
    val cityName = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CITY_NAME)] ?: ""
    val conditionRating = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING)] ?: ""
    val conditionDisplay = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING_DISPLAY)] ?: ""
    val temperature = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_TEMPERATURE)] ?: ""
    val activitiesJson = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ACTIVITIES_JSON)] ?: ""
    val errorMessage = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ERROR_MESSAGE)]
    val lastUpdated = prefs[longPreferencesKey(WidgetStateSerializer.KEY_LAST_UPDATED)] ?: 0L

    val isLoading = lastUpdated == 0L && errorMessage == null
    val hasError = errorMessage != null
    val activities = WidgetStateSerializer.deserializeActivities(activitiesJson)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        when {
            isLoading -> WidgetLoadingContent()
            hasError -> WidgetErrorContent(errorMessage!!)
            else -> TodayForecastBody(cityName, temperature, conditionRating, conditionDisplay, activities)
        }
    }
}

@Composable
private fun TodayForecastBody(
    cityName: String,
    temperature: String,
    conditionRating: String,
    conditionDisplay: String,
    activities: List<ActivityState>
) {
    if (activities.isEmpty()) return
    val primary = activities.firstOrNull { it.isPrimary } ?: activities.first()
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp)
    ) {
        // Header: city name + condition badge + temperature
        WidgetHeader(cityName) {
            ConditionBadge(conditionRating, conditionDisplay)
            Spacer(modifier = GlanceModifier.size(6.dp))
            Text(
                text = temperature,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }

        Spacer(modifier = GlanceModifier.size(8.dp))

        // Full-width hero card
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .background(GlanceTheme.colors.surfaceVariant)
                .cornerRadius(12.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrimarySportHero(activity = primary, iconSize = 44.dp, nameFontSize = 18f, showReason = true)
        }
    }
}
