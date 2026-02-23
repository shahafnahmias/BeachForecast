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
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.beachforecast.MainActivity
import io.beachforecast.R
import io.beachforecast.widget.theme.BeachForecastWidgetTheme
import io.beachforecast.widget.theme.ConditionColors

class LargeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BeachForecastWidgetTheme {
                LargeWidgetContent()
            }
        }
    }
}

class LargeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeWidget()
}

@Composable
private fun LargeWidgetContent() {
    val context = LocalContext.current
    val prefs = currentState<Preferences>()
    val cityName = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CITY_NAME)] ?: ""
    val temperature = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_TEMPERATURE)] ?: ""
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
            isLoading -> LoadingContent()
            hasError -> ErrorContent(errorMessage!!)
            else -> MultiDayContent(cityName, temperature, dayForecasts)
        }
    }
}

@Composable
private fun MultiDayContent(
    cityName: String,
    temperature: String,
    dayForecasts: List<DayForecastState>
) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16.dp)
    ) {
        // Header: icon + beach name + temperature
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = GlanceModifier.size(16.dp)
            )
            Spacer(modifier = GlanceModifier.size(4.dp))
            Text(
                text = cityName,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = temperature,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }

        Spacer(modifier = GlanceModifier.size(8.dp))

        // Day forecast rows
        dayForecasts.forEachIndexed { index, day ->
            if (index > 0) Spacer(modifier = GlanceModifier.size(4.dp))
            DayForecastRow(
                day = day,
                isToday = index == 0,
                modifier = GlanceModifier.fillMaxWidth().defaultWeight()
            )
        }
    }
}

@Composable
private fun DayForecastRow(
    day: DayForecastState,
    isToday: Boolean,
    modifier: GlanceModifier
) {
    val conditionColor = ConditionColors.forRating(day.conditionRating)
    val cardBackground = if (isToday) {
        GlanceTheme.colors.surfaceVariant
    } else {
        GlanceTheme.colors.widgetBackground
    }

    Column(
        modifier = modifier
            .background(cardBackground)
            .cornerRadius(12.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Day name + condition rating
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day.dayName,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = day.conditionRatingDisplay,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(conditionColor)
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.size(4.dp))

        // Condition color bar
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(4.dp)
                .background(ColorProvider(conditionColor))
                .cornerRadius(2.dp)
        ) {}

        Spacer(modifier = GlanceModifier.size(4.dp))

        // Metrics row: wave, wind, temp
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day.waveHeightFormatted,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = day.windFormatted,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = day.temperatureFormatted,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            provider = ImageProvider(R.mipmap.ic_launcher),
            contentDescription = null,
            modifier = GlanceModifier.size(24.dp)
        )
        Spacer(modifier = GlanceModifier.size(8.dp))
        Text(
            text = "Loading...",
            style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant)
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            provider = ImageProvider(R.mipmap.ic_launcher),
            contentDescription = null,
            modifier = GlanceModifier.size(24.dp)
        )
        Spacer(modifier = GlanceModifier.size(8.dp))
        Text(
            text = message,
            style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant),
            maxLines = 2
        )
    }
}
