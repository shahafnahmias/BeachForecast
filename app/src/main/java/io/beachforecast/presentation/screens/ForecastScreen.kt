package io.beachforecast.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Surfing
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.beachforecast.R
import io.beachforecast.data.preferences.UserPreferences
import io.beachforecast.domain.calculators.ActivityRecommendationCalculator
import io.beachforecast.domain.calculators.BestDayCalculator
import io.beachforecast.domain.formatters.CompactWeatherFormatter
import io.beachforecast.domain.formatters.WeatherFormatter
import io.beachforecast.domain.models.Activity
import io.beachforecast.domain.models.ActivityRecommendations
import io.beachforecast.domain.models.UnitConverter
import io.beachforecast.domain.models.UnitSystem
import io.beachforecast.presentation.components.GlassCard
import io.beachforecast.presentation.components.GlassCardNoPadding
import io.beachforecast.presentation.components.StitchSectionHeader
import io.beachforecast.presentation.components.getCategoryColor
import io.beachforecast.presentation.models.DayForecastUiData
import io.beachforecast.presentation.models.ThreeHourPeriodUiData
import io.beachforecast.presentation.models.WeatherUiState

import io.beachforecast.ui.theme.StitchTheme

/**
 * Detailed forecast screen showing 7-day forecast
 */
@Composable
fun ForecastScreen(
    uiState: WeatherUiState,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is WeatherUiState.Initial, is WeatherUiState.Loading -> {
            ForecastLoadingSkeleton(modifier = modifier)
        }

        is WeatherUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.displayMedium
                        )
                        val context = LocalContext.current
                        Text(
                            text = context.getString(uiState.error.getShortMessageRes()),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = context.getString(uiState.error.getUserMessageRes()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        is WeatherUiState.Success -> {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300))
            ) {
                ForecastContent(uiState = uiState, modifier = modifier)
            }
        }
    }
}

private fun getDayLabel(
    context: android.content.Context,
    index: Int,
    dayName: String
): String {
    return when (index) {
        0 -> context.getString(R.string.forecast_today)
        1 -> context.getString(R.string.forecast_tomorrow)
        else -> dayName.uppercase()
    }
}

@Composable
private fun ForecastContent(
    uiState: WeatherUiState.Success,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val selectedSports by userPreferences.selectedSportsFlow.collectAsState(initial = Activity.getDefaults())
    val unitSystem by userPreferences.unitSystemFlow.collectAsState(initial = UnitSystem.METRIC)
    var showHourlyView by rememberSaveable { mutableStateOf(false) }
    var expandedDayIndex by rememberSaveable { mutableStateOf(0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ForecastHeader(
                title = stringResource(id = R.string.forecast_detailed_title),
                showHourlyView = showHourlyView,
                onToggleHourly = { showHourlyView = !showHourlyView }
            )
        }

        // Location pill
        item {
            LocationPill(
                beachName = uiState.data.location.cityName,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            )
        }

        // Best day per sport card
        item {
            val bestDays = remember(uiState.data.weekForecast, selectedSports) {
                BestDayCalculator.findBestDayPerSport(uiState.data.weekForecast, selectedSports)
            }
            if (bestDays.isNotEmpty()) {
                BestDayForSportCard(
                    bestDays = bestDays,
                    weekForecast = uiState.data.weekForecast,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        if (uiState.data.weekForecast.isEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.empty_no_forecast_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        uiState.data.weekForecast.forEachIndexed { index, dayForecast ->
            item {
                val recommendations = remember(dayForecast, selectedSports) {
                    BestDayCalculator.calculateDayActivityRecommendations(dayForecast, selectedSports)
                }
                val isExpanded = expandedDayIndex == index

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(
                        animationSpec = tween(300, delayMillis = index * 80)
                    ) + slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = tween(400, delayMillis = index * 80, easing = FastOutSlowInEasing)
                    ),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    if (showHourlyView) {
                        HourlyDayForecastCard(
                            dayForecast = dayForecast,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        ForecastDayRow(
                            dayIndex = index,
                            dayForecast = dayForecast,
                            recommendations = recommendations,
                            unitSystem = unitSystem,
                            isExpanded = isExpanded,
                            onToggle = {
                                expandedDayIndex = if (isExpanded) -1 else index
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        // Footer attribution
        item {
            Text(
                text = stringResource(id = R.string.forecast_data_attribution),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ForecastHeader(
    title: String,
    showHourlyView: Boolean,
    onToggleHourly: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StitchSectionHeader(
            title = title,
            modifier = Modifier.weight(1f),
            actionText = null
        )

        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleHourly()
            },
            modifier = Modifier
                .size(40.dp)
                .animateContentSize()
        ) {
            Icon(
                imageVector = if (showHourlyView) Icons.Filled.Schedule else Icons.Filled.CalendarMonth,
                contentDescription = if (showHourlyView) {
                    stringResource(R.string.forecast_show_daily)
                } else {
                    stringResource(R.string.forecast_show_hourly)
                },
                tint = if (showHourlyView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun LocationPill(
    beachName: String,
    modifier: Modifier = Modifier
) {
    val colors = StitchTheme.colors

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(colors.glassBackground)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = stringResource(R.string.cd_location_icon),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = beachName,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun ForecastDayRow(
    dayIndex: Int,
    dayForecast: DayForecastUiData,
    recommendations: ActivityRecommendations,
    unitSystem: UnitSystem,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dayLabel = getDayLabel(context, dayIndex, dayForecast.dayName)
    val dayAverage = remember(dayForecast) { BestDayCalculator.calculateDayAverageConditions(dayForecast) }
    val waveColor = getCategoryColor(dayAverage.waveCategory)
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "chevronRotation"
    )

    GlassCardNoPadding(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row (always visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = StitchTheme.colors.textSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dayForecast.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = UnitConverter.formatWaveHeightRange(
                            dayAverage.waveHeight,
                            unitSystem
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = waveColor,
                        fontWeight = FontWeight.Bold
                    )

                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.cd_expand_collapse),
                        tint = StitchTheme.colors.textSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronRotation)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 2x2 vitals grid
                    ForecastVitalsGrid(dayAverage = dayAverage, unitSystem = unitSystem)

                    // Best For section
                    BestForRow(
                        recommendations = recommendations
                    )
                }
            }
        }
    }
}

// DayAverageConditions, calculateDayAverageConditions, calculateDayActivityRecommendations,
// and findBestDayPerSport extracted to BestDayCalculator for reuse.

@Composable
private fun BestDayForSportCard(
    bestDays: Map<Activity, Int>,
    weekForecast: List<DayForecastUiData>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sort by Activity ordinal for consistent ordering
            bestDays.entries.sortedBy { it.key.ordinal }.forEach { (activity, dayIndex) ->
                val dayForecast = weekForecast.getOrNull(dayIndex) ?: return@forEach
                val dayLabel = getDayLabel(context, dayIndex, dayForecast.dayName)

                val icon = when (activity) {
                    Activity.SWIM -> Icons.Default.Pool
                    Activity.SURF -> Icons.Default.Surfing
                    Activity.KITE -> Icons.Default.Air
                    Activity.SUP -> Icons.Default.Surfing
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = context.getString(activity.nameRes),
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(
                            R.string.forecast_best_day_for,
                            context.getString(activity.nameRes)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastVitalsGrid(
    dayAverage: BestDayCalculator.DayAverageConditions,
    unitSystem: UnitSystem = UnitSystem.METRIC,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ForecastVitalCard(
                icon = Icons.Default.Air,
                label = stringResource(id = R.string.forecast_wind),
                value = buildWindValue(dayAverage, unitSystem),
                modifier = Modifier.weight(1f)
            )
            ForecastVitalCard(
                icon = Icons.Default.Waves,
                label = stringResource(id = R.string.forecast_swell),
                value = buildSwellValue(dayAverage, unitSystem),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ForecastVitalCard(
                icon = Icons.Default.Thermostat,
                label = stringResource(id = R.string.forecast_sea_temp),
                value = if (dayAverage.seaSurfaceTemperature > 0) {
                    WeatherFormatter.formatSeaTemperature(
                        dayAverage.seaSurfaceTemperature,
                        unitSystem
                    )
                } else {
                    "—"
                },
                modifier = Modifier.weight(1f)
            )
            ForecastVitalCard(
                icon = Icons.Default.WbSunny,
                label = stringResource(id = R.string.forecast_uv_index),
                value = if (dayAverage.uvIndex > 0) {
                    WeatherFormatter.formatUvIndex(dayAverage.uvIndex)
                } else {
                    "—"
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ForecastVitalCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = StitchTheme.colors

    GlassCard(
        modifier = modifier,
        cornerRadius = 12.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                letterSpacing = 0.5.sp
            )
        }
    }
}

private fun buildWindValue(dayAverage: BestDayCalculator.DayAverageConditions, unitSystem: UnitSystem = UnitSystem.METRIC): String {
    if (dayAverage.windSpeed <= 0) return "—"
    val (text, _) = CompactWeatherFormatter.formatWindCompact(
        directionDegrees = dayAverage.windDirection.toDouble(),
        speedKmh = dayAverage.windSpeed,
        unitSystem = unitSystem
    )
    return text
}

private fun buildSwellValue(dayAverage: BestDayCalculator.DayAverageConditions, unitSystem: UnitSystem = UnitSystem.METRIC): String {
    if (dayAverage.swellHeight <= 0) return "—"
    val (text, _) = CompactWeatherFormatter.formatSwellCompact(
        directionDegrees = dayAverage.swellDirection.toDouble(),
        heightMeters = dayAverage.swellHeight,
        periodSeconds = dayAverage.swellPeriod,
        unitSystem = unitSystem
    )
    return text
}

@Composable
private fun BestForRow(
    recommendations: ActivityRecommendations,
    modifier: Modifier = Modifier
) {
    val colors = StitchTheme.colors

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(id = R.string.forecast_best_for),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            recommendations.recommendations.forEach { rec ->
                ActivityIcon(
                    activity = rec.activity,
                    isRecommended = rec.isRecommended,
                    isPrimary = rec.isPrimary,
                    modifier = Modifier
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActivityIcon(
    activity: Activity,
    isRecommended: Boolean,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = StitchTheme.colors
    val primaryColor = MaterialTheme.colorScheme.primary

    val icon = when (activity) {
        Activity.SWIM -> Icons.Default.Pool
        Activity.SURF -> Icons.Default.Surfing
        Activity.KITE -> Icons.Default.Air
        Activity.SUP -> Icons.Default.Surfing
    }

    val iconColor = if (isRecommended) primaryColor else colors.slate
    val textColor = if (isRecommended) primaryColor else colors.slate

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isPrimary -> primaryColor.copy(alpha = 0.2f)
                        isRecommended -> colors.glassBackground
                        else -> colors.glassBackground
                    }
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = context.getString(activity.nameRes),
                tint = iconColor.copy(alpha = if (isRecommended) 1f else 0.4f),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = context.getString(activity.nameRes).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = if (isRecommended) 1f else 0.6f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HourlyDayForecastCard(
    dayForecast: DayForecastUiData,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Keep the day name header
            Text(
                text = dayForecast.dayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Show 3-hour periods horizontally for this day
            if (dayForecast.hourlyPeriods.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(dayForecast.hourlyPeriods, key = { it.timeLabel }) { period ->
                        ThreeHourPeriodCard(period)
                    }
                }
            } else {
                Text(
                    text = context.getString(R.string.forecast_no_hourly_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ThreeHourPeriodCard(
    period: ThreeHourPeriodUiData,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(period.waveCategory)
    val timeColor = if (period.isNow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier.width(110.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (period.isNow) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (period.isNow) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        } else {
            androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Time label
            Text(
                text = period.timeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = timeColor,
                fontWeight = FontWeight.Bold
            )

            // Wave height in CM with color coding
            Text(
                text = period.waveHeightFormatted,
                style = MaterialTheme.typography.titleMedium,
                color = categoryColor,
                fontWeight = FontWeight.Bold
            )

            // Temperature
            Text(
                text = period.temperatureFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Cloud cover emoji
            Text(
                text = WeatherFormatter.formatCloudCover(period.cloudCover),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 24.sp
            )
        }
    }
}

@Composable
private fun ForecastLoadingSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header skeleton
        item {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
            )
        }

        // Forecast cards skeleton
        items(7) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
                            )
                        }
                    }
                }
            }
        }
    }
}
