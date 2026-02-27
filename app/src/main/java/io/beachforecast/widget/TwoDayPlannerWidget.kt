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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
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
import io.beachforecast.widget.components.SecondaryDotRow
import io.beachforecast.widget.components.WidgetErrorContent
import io.beachforecast.widget.components.WidgetHeader
import io.beachforecast.widget.components.WidgetLoadingContent
import io.beachforecast.widget.components.primaryAndSecondaries
import io.beachforecast.widget.theme.BeachForecastWidgetTheme

class TwoDayPlannerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BeachForecastWidgetTheme {
                TwoDayPlannerContent()
            }
        }
    }
}

class TwoDayPlannerWidgetReceiver : BeachForecastWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TwoDayPlannerWidget()
}

@Composable
private fun TwoDayPlannerContent() {
    val context = LocalContext.current
    val prefs = currentState<Preferences>()
    val cityName = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CITY_NAME)] ?: ""
    val dayForecastsJson = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_DAY_FORECASTS_JSON)] ?: ""
    val errorMessage = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ERROR_MESSAGE)]
    val lastUpdated = prefs[longPreferencesKey(WidgetStateSerializer.KEY_LAST_UPDATED)] ?: 0L

    val isLoading = lastUpdated == 0L && errorMessage == null
    val hasError = errorMessage != null
    val dayForecasts = WidgetStateSerializer.deserializeDayForecasts(dayForecastsJson)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        when {
            isLoading -> WidgetLoadingContent()
            hasError -> WidgetErrorContent(errorMessage!!)
            dayForecasts.size >= 2 -> TwoDayBody(cityName, dayForecasts[0], dayForecasts[1])
            else -> WidgetErrorContent("No forecast data")
        }
    }
}

@Composable
private fun TwoDayBody(
    cityName: String,
    today: DayForecastState,
    tomorrow: DayForecastState
) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp)
    ) {
        // Header
        WidgetHeader(cityName)

        Spacer(modifier = GlanceModifier.size(6.dp))

        // Two columns side-by-side
        Row(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight()
        ) {
            DayColumn(
                day = today,
                isHighlighted = true,
                modifier = GlanceModifier.defaultWeight()
            )
            Spacer(modifier = GlanceModifier.size(6.dp))
            DayColumn(
                day = tomorrow,
                isHighlighted = false,
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }
}

@Composable
private fun DayColumn(
    day: DayForecastState,
    isHighlighted: Boolean,
    modifier: GlanceModifier
) {
    val background = if (isHighlighted) {
        GlanceTheme.colors.surfaceVariant
    } else {
        GlanceTheme.colors.widgetBackground
    }

    if (day.activities.isEmpty()) return
    val (primary, secondaries) = day.activities.primaryAndSecondaries()

    Column(
        modifier = modifier
            .background(background)
            .cornerRadius(12.dp)
            .padding(8.dp)
    ) {
        // Day name + condition badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = day.dayName,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            Spacer(modifier = GlanceModifier.size(4.dp))
            ConditionBadge(day.conditionRating, day.conditionRatingDisplay, fontSize = 11f)
        }

        Spacer(modifier = GlanceModifier.size(4.dp))

        // Primary sport hero
        PrimarySportHero(activity = primary, iconSize = 32.dp, nameFontSize = 12f, showReason = true)

        if (secondaries.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.size(4.dp))
            SecondaryDotRow(activities = secondaries, dotSize = 6.dp, showNames = false)
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Wave height + wind metrics
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = day.waveHeightFormatted,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = day.windFormatted,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}
