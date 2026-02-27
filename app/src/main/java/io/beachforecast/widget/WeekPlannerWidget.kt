package io.beachforecast.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.unit.ColorProvider
import io.beachforecast.MainActivity
import io.beachforecast.widget.components.WidgetErrorContent
import io.beachforecast.widget.components.WidgetHeader
import io.beachforecast.widget.components.WidgetLoadingContent
import io.beachforecast.widget.components.sportIconRes
import io.beachforecast.widget.theme.BeachForecastWidgetTheme
import io.beachforecast.widget.theme.ConditionColors

private val RecommendedColor = Color(0xFF4CAF50)
private val NotRecommendedColor = Color(0xFF78909C)

class WeekPlannerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BeachForecastWidgetTheme {
                WeekPlannerContent()
            }
        }
    }
}

class WeekPlannerWidgetReceiver : BeachForecastWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeekPlannerWidget()
}

@Composable
private fun WeekPlannerContent() {
    val context = LocalContext.current
    val prefs = currentState<Preferences>()
    val cityName = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CITY_NAME)] ?: ""
    val weekForecastsJson = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_WEEK_FORECASTS_JSON)] ?: ""
    val bestDayIndex = prefs[intPreferencesKey(WidgetStateSerializer.KEY_BEST_DAY_INDEX)] ?: -1
    val bestDaySummary = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_BEST_DAY_SUMMARY)] ?: ""
    val errorMessage = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ERROR_MESSAGE)]
    val lastUpdated = prefs[longPreferencesKey(WidgetStateSerializer.KEY_LAST_UPDATED)] ?: 0L

    val isLoading = lastUpdated == 0L && errorMessage == null
    val hasError = errorMessage != null
    val weekForecasts = WidgetStateSerializer.deserializeDayForecasts(weekForecastsJson)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        when {
            isLoading -> WidgetLoadingContent()
            hasError -> WidgetErrorContent(errorMessage!!)
            weekForecasts.isEmpty() -> WidgetErrorContent("No forecast data")
            else -> WeekPlannerBody(cityName, weekForecasts, bestDayIndex, bestDaySummary)
        }
    }
}

@Composable
private fun WeekPlannerBody(
    cityName: String,
    days: List<DayForecastState>,
    bestDayIndex: Int,
    bestDaySummary: String
) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp)
    ) {
        // Header: city name + "Week Forecast"
        WidgetHeader(cityName) {
            Text(
                text = "Week Forecast",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = GlanceModifier.size(6.dp))

        // Day columns grid
        Row(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight()
        ) {
            days.forEachIndexed { index, day ->
                if (index > 0) Spacer(modifier = GlanceModifier.size(2.dp))
                DayColumn(
                    day = day,
                    isBestDay = index == bestDayIndex,
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }

        Spacer(modifier = GlanceModifier.size(6.dp))

        // Best day footer
        if (bestDaySummary.isNotEmpty()) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(8.dp)
                    .padding(8.dp)
            ) {
                Text(
                    text = bestDaySummary,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onSurface
                    ),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun DayColumn(
    day: DayForecastState,
    isBestDay: Boolean,
    modifier: GlanceModifier
) {
    val primary = day.activities.firstOrNull { it.isPrimary } ?: day.activities.firstOrNull()
    val conditionColor = ConditionColors.forRating(day.conditionRating)
    val tintColor = if (primary?.isRecommended == true) RecommendedColor else NotRecommendedColor

    Column(
        modifier = modifier
            .let {
                if (isBestDay) it.background(GlanceTheme.colors.surfaceVariant).cornerRadius(6.dp)
                else it
            }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day name
        Text(
            text = day.dayName.take(3),
            style = TextStyle(
                fontSize = 9.sp,
                fontWeight = if (isBestDay) FontWeight.Bold else FontWeight.Normal,
                color = GlanceTheme.colors.onSurfaceVariant
            ),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.size(4.dp))

        // Primary sport icon in tinted circle
        if (primary != null) {
            Box(
                modifier = GlanceModifier
                    .size(36.dp)
                    .background(ColorProvider(tintColor.copy(alpha = 0.15f)))
                    .cornerRadius(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(sportIconRes(primary.activityKey)),
                    contentDescription = primary.name,
                    modifier = GlanceModifier.size(24.dp),
                    colorFilter = ColorFilter.tint(ColorProvider(tintColor))
                )
            }
        }

        Spacer(modifier = GlanceModifier.size(4.dp))

        // Condition color dot
        Box(
            modifier = GlanceModifier
                .size(10.dp)
                .background(ColorProvider(conditionColor))
                .cornerRadius(100.dp)
        ) {}
    }
}
