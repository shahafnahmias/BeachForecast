package io.beachforecast.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Surfing
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.beachforecast.R
import io.beachforecast.domain.models.Activity
import io.beachforecast.domain.models.ActivityRecommendation
import io.beachforecast.domain.models.ActivityRecommendations
import io.beachforecast.domain.models.ConditionRating
import io.beachforecast.presentation.models.DayForecastUiData
import io.beachforecast.ui.theme.StitchTheme

/**
 * Location header with beach name and live status indicator.
 */
@Composable
fun LocationHeader(
    beachName: String,
    lastUpdatedText: String,
    modifier: Modifier = Modifier,
    onLocationClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Location selector
        Row(
            modifier = Modifier.clickable { onLocationClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = beachName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.home_select_beach),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Live status badge
        LiveStatusBadge(text = lastUpdatedText)
    }
}

/**
 * Best For card showing activity recommendations.
 */
@Composable
fun BestForCard(
    recommendations: ActivityRecommendations,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.home_current_status),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_best_for),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Condition badge
                ConditionBadge(text = stringResource(recommendations.conditionRating.nameRes))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Activity icons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                recommendations.recommendations.forEach { rec ->
                    ActivityIconItem(
                        recommendation = rec,
                        isPrimary = rec.isPrimary
                    )
                }
            }
        }
    }
}

/**
 * Single activity icon with label.
 */
@Composable
private fun ActivityIconItem(
    recommendation: ActivityRecommendation,
    isPrimary: Boolean
) {
    val context = LocalContext.current
    val colors = StitchTheme.colors
    val primaryColor = MaterialTheme.colorScheme.primary

    val icon = when (recommendation.activity) {
        Activity.SWIM -> Icons.Default.Pool
        Activity.SURF -> Icons.Default.Surfing
        Activity.KITE -> Icons.Default.Air
        Activity.SUP -> Icons.Default.Surfing // Using surfing as placeholder
    }

    val isActive = recommendation.isRecommended
    val iconColor = if (isActive) primaryColor else colors.slate
    val textColor = if (isActive) primaryColor else colors.slate

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .then(
                    if (isPrimary) {
                        Modifier
                            .drawBehind {
                                // Glow effect for primary
                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.3f),
                                    radius = size.minDimension / 2 + 4.dp.toPx()
                                )
                            }
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.2f))
                            .border(1.dp, primaryColor.copy(alpha = 0.4f), CircleShape)
                    } else if (isActive) {
                        Modifier
                            .clip(CircleShape)
                            .background(colors.glassBackground)
                            .border(1.dp, colors.glassBorder, CircleShape)
                    } else {
                        Modifier
                            .clip(CircleShape)
                            .background(colors.glassBackground)
                            .border(1.dp, colors.glassBorder, CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = context.getString(recommendation.activity.nameRes),
                tint = iconColor.copy(alpha = if (isActive) 1f else 0.4f),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = context.getString(recommendation.activity.nameRes).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = if (isActive) 1f else 0.6f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Data class for hourly condition display.
 */
data class HourlyConditionData(
    val time: String,
    val waveHeightRange: String,
    val isNow: Boolean = false,
    val weatherIcon: ImageVector = Icons.Default.WbSunny
)

/**
 * Horizontal scrollable row of hourly condition cards.
 */
@Composable
fun TodaysConditionsRow(
    hourlyData: List<HourlyConditionData>,
    modifier: Modifier = Modifier,
    onViewChartClick: () -> Unit = {}
) {
    Column(modifier = modifier) {
        // Section header
        StitchSectionHeader(
            title = stringResource(R.string.home_todays_conditions),
            actionText = stringResource(R.string.home_view_chart),
            onActionClick = onViewChartClick
        )

        if (hourlyData.isEmpty()) {
            Text(
                text = stringResource(R.string.empty_no_conditions_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            // Horizontal scroll row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(hourlyData, key = { it.time }) { data ->
                    HourlyConditionCard(data = data)
                }
            }
        }
    }
}

/**
 * Single hourly condition card.
 */
@Composable
private fun HourlyConditionCard(
    data: HourlyConditionData
) {
    val colors = StitchTheme.colors
    val primaryColor = MaterialTheme.colorScheme.primary

    if (data.isNow) {
        // Active/NOW card
        ActiveCardNoPadding(
            modifier = Modifier.width(140.dp),
            cornerRadius = 12.dp
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                // NOW badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(4.dp))
                        .background(primaryColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_now),
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = data.time,
                            style = MaterialTheme.typography.bodyMedium,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = data.weatherIcon,
                            contentDescription = stringResource(R.string.cd_weather_icon),
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = data.waveHeightRange,
                        style = MaterialTheme.typography.titleLarge,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        // Regular card
        GlassCardNoPadding(
            modifier = Modifier
                .width(140.dp)
                .then(
                    // Slightly fade past hours
                    Modifier
                ),
            cornerRadius = 12.dp
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = data.time,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                        Icon(
                            imageVector = data.weatherIcon,
                            contentDescription = stringResource(R.string.cd_weather_icon),
                            tint = colors.amber,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = data.waveHeightRange,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Weekly best day card showing best day per sport.
 */
@Composable
fun WeeklyBestDayCard(
    bestDays: Map<Activity, Int>,
    weekForecast: List<DayForecastUiData>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier) {
        StitchSectionHeader(title = stringResource(R.string.home_best_days_this_week))

        if (bestDays.isEmpty()) {
            Text(
                text = stringResource(R.string.empty_no_best_day_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 16.dp
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    bestDays.entries.sortedBy { it.key.ordinal }.forEach { (activity, dayIndex) ->
                        val dayForecast = weekForecast.getOrNull(dayIndex) ?: return@forEach
                        val dayLabel = when (dayIndex) {
                            0 -> context.getString(R.string.forecast_today)
                            1 -> context.getString(R.string.forecast_tomorrow)
                            else -> dayForecast.dayName.uppercase()
                        }

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
                                    R.string.home_best_day_for,
                                    context.getString(activity.nameRes)
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data class for vital metrics.
 */
data class VitalData(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val secondaryIcon: ImageVector? = null, // For wind direction
    val accentColor: Color? = null
)

/**
 * 2x2 grid of vital metrics.
 */
@Composable
fun LiveVitalsGrid(
    vitals: List<VitalData>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        StitchSectionHeader(title = stringResource(R.string.home_live_vitals))

        if (vitals.isEmpty()) {
            Text(
                text = stringResource(R.string.empty_no_vitals_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                vitals.chunked(2).forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { vital ->
                            VitalCard(data = vital, modifier = Modifier.weight(1f))
                        }
                        // Add spacer if odd item in last row to maintain consistent sizing
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single vital metric card.
 */
@Composable
private fun VitalCard(
    data: VitalData,
    modifier: Modifier = Modifier
) {
    val colors = StitchTheme.colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val iconColor = data.accentColor ?: primaryColor

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
                    imageVector = data.icon,
                    contentDescription = stringResource(R.string.cd_vital_icon, data.label),
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                data.secondaryIcon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(R.string.cd_wind_direction_icon),
                        tint = colors.textTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = data.value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = data.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                letterSpacing = 0.5.sp
            )
        }
    }
}

