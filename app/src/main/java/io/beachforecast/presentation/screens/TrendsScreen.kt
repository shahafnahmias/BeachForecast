package io.beachforecast.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import io.beachforecast.R
import io.beachforecast.presentation.components.ActiveCard
import io.beachforecast.presentation.components.ActiveCardNoPadding
import io.beachforecast.presentation.components.ConditionBadge
import io.beachforecast.presentation.components.GlassCard
import io.beachforecast.presentation.components.GlassCardNoPadding
import io.beachforecast.presentation.components.StitchSectionHeader
import io.beachforecast.domain.models.TideType
import io.beachforecast.presentation.models.DayForecastUiData
import io.beachforecast.presentation.models.HourlyForecastUiData
import io.beachforecast.presentation.models.TideEventUiData
import io.beachforecast.presentation.models.WeatherUiData
import io.beachforecast.presentation.models.WeatherUiState
import io.beachforecast.ui.theme.StitchAmber
import io.beachforecast.ui.theme.StitchEmerald
import io.beachforecast.ui.theme.StitchPrimary
import io.beachforecast.ui.theme.StitchTheme
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf

/**
 * Trends screen showing 24h ocean analytics using Vico charts.
 */
@Composable
fun TrendsScreen(
    uiState: WeatherUiState,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is WeatherUiState.Initial,
        is WeatherUiState.Loading -> {
            TrendsLoadingSkeleton(modifier)
        }

        is WeatherUiState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        val context = LocalContext.current
                        Text(
                            text = context.getString(uiState.error.getShortMessageRes()),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = context.getString(uiState.error.getUserMessageRes()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        is WeatherUiState.Success -> {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing))
            ) {
                TrendsContent(data = uiState.data, modifier = modifier)
            }
        }
    }
}

@Composable
private fun TrendsContent(
    data: WeatherUiData,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by rememberSaveable { mutableIntStateOf(0) }
    val hourly = data.weekHourly.getOrElse(selectedDayIndex) { data.todayForecast.hourlyForecast }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TrendsHeader(
                title = stringResource(id = R.string.trends_title)
            )
        }

        item {
            LocationSubtitle(beachName = data.location.cityName)
        }

        item {
            DayPickerRow(
                days = data.weekForecast,
                selectedIndex = selectedDayIndex,
                onDaySelected = { selectedDayIndex = it }
            )
        }

        item {
            TideScheduleSection(tideEvents = data.tideEvents)
        }

        item {
            SwellAnalysisChart(hourly = hourly)
        }

        item {
            WaveHeightTrendChart(hourly = hourly)
        }

        item {
            WavePeriodTrendChart(hourly = hourly)
        }

        item {
            WindSpeedTrendChart(hourly = hourly)
        }

        item {
            WindDirectionSection(hourly = hourly)
        }

        item {
            AttributionFooter()
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun DayPickerRow(
    days: List<DayForecastUiData>,
    selectedIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val colors = StitchTheme.colors

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(days) { index, day ->
            val label = when (index) {
                0 -> context.getString(R.string.forecast_today)
                1 -> context.getString(R.string.forecast_tomorrow)
                else -> day.dayName.uppercase()
            }
            val isSelected = index == selectedIndex

            if (isSelected) {
                ActiveCardNoPadding(
                    modifier = Modifier
                        .clickable { onDaySelected(index) },
                    cornerRadius = 50.dp,
                    glowIntensity = 0.2f
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                GlassCardNoPadding(
                    modifier = Modifier
                        .clickable { onDaySelected(index) },
                    cornerRadius = 50.dp
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendsHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = stringResource(R.string.cd_trend_icon),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun LocationSubtitle(beachName: String) {
    Text(
        text = beachName.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 2.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun SwellAnalysisChart(hourly: List<HourlyForecastUiData>) {
    val colors = StitchTheme.colors
    val entries = hourly.mapIndexed { index, h ->
        entryOf(index.toFloat(), h.swellHeight.toFloat())
    }
    val model = entryModelOf(entries)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        cornerRadius = 16.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(id = R.string.trends_swell_analysis).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                ConditionBadge(
                    text = androidx.compose.ui.res.stringResource(id = R.string.trends_height_distribution),
                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    textColor = MaterialTheme.colorScheme.primary
                )
            }

            if (entries.isNotEmpty()) {
                Chart(
                    chart = columnChart(),
                    model = model,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = { value, _ ->
                            val index = value.toInt()
                            if (index in hourly.indices && index % 6 == 0) {
                                hourly[index].timeFormatted
                            } else ""
                        }
                    ),
                    modifier = Modifier.height(200.dp)
                )
            } else {
                Text(
                    text = androidx.compose.ui.res.stringResource(id = R.string.empty_no_chart_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun WaveHeightTrendChart(hourly: List<HourlyForecastUiData>) {
    val colors = StitchTheme.colors
    val entries = hourly.mapIndexed { index, h ->
        entryOf(index.toFloat(), h.waveHeight.toFloat())
    }
    val model = entryModelOf(entries)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        cornerRadius = 16.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(id = R.string.trends_wave_height).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                ConditionBadge(
                    text = androidx.compose.ui.res.stringResource(id = R.string.trends_meters),
                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    textColor = MaterialTheme.colorScheme.primary
                )
            }

            if (entries.isNotEmpty()) {
                Chart(
                    chart = lineChart(
                        lines = listOf(
                            lineSpec(
                                lineColor = StitchPrimary,
                                lineBackgroundShader = DynamicShaders.fromBrush(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            StitchPrimary.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    model = model,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = { value, _ ->
                            val index = value.toInt()
                            if (index in hourly.indices && index % 6 == 0) {
                                hourly[index].timeFormatted
                            } else ""
                        }
                    ),
                    modifier = Modifier.height(200.dp)
                )
            } else {
                Text(
                    text = androidx.compose.ui.res.stringResource(id = R.string.empty_no_chart_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun WavePeriodTrendChart(hourly: List<HourlyForecastUiData>) {
    val colors = StitchTheme.colors
    val entries = hourly.mapIndexed { index, h ->
        entryOf(index.toFloat(), h.wavePeriod.toFloat())
    }
    val model = entryModelOf(entries)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        cornerRadius = 16.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.trends_wave_period).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                ConditionBadge(
                    text = stringResource(id = R.string.trends_seconds_unit),
                    backgroundColor = StitchAmber.copy(alpha = 0.1f),
                    borderColor = StitchAmber.copy(alpha = 0.2f),
                    textColor = StitchAmber
                )
            }

            if (entries.isNotEmpty()) {
                Chart(
                    chart = lineChart(
                        lines = listOf(
                            lineSpec(
                                lineColor = StitchAmber,
                                lineBackgroundShader = DynamicShaders.fromBrush(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            StitchAmber.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    model = model,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = { value, _ ->
                            val index = value.toInt()
                            if (index in hourly.indices && index % 6 == 0) {
                                hourly[index].timeFormatted
                            } else ""
                        }
                    ),
                    modifier = Modifier.height(200.dp)
                )
            } else {
                Text(
                    text = androidx.compose.ui.res.stringResource(id = R.string.empty_no_chart_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun WindSpeedTrendChart(hourly: List<HourlyForecastUiData>) {
    val colors = StitchTheme.colors
    val entries = hourly.mapIndexed { index, h ->
        entryOf(index.toFloat(), h.windSpeed.toFloat())
    }
    val model = entryModelOf(entries)

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        cornerRadius = 16.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(id = R.string.trends_wind_speed).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                ConditionBadge(
                    text = androidx.compose.ui.res.stringResource(id = R.string.trends_kts_kmh),
                    backgroundColor = StitchEmerald.copy(alpha = 0.1f),
                    borderColor = StitchEmerald.copy(alpha = 0.2f),
                    textColor = StitchEmerald
                )
            }

            if (entries.isNotEmpty()) {
                Chart(
                    chart = lineChart(
                        lines = listOf(
                            lineSpec(
                                lineColor = StitchEmerald,
                                lineBackgroundShader = DynamicShaders.fromBrush(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            StitchEmerald.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    model = model,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = { value, _ ->
                            val index = value.toInt()
                            if (index in hourly.indices && index % 6 == 0) {
                                hourly[index].timeFormatted
                            } else ""
                        }
                    ),
                    modifier = Modifier.height(200.dp)
                )
            } else {
                Text(
                    text = androidx.compose.ui.res.stringResource(id = R.string.empty_no_chart_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun TideScheduleSection(tideEvents: List<TideEventUiData>) {
    val colors = StitchTheme.colors

    Column {
        StitchSectionHeader(
            title = stringResource(R.string.trends_tide_schedule)
        )

        if (tideEvents.isEmpty()) {
            Text(
                text = stringResource(R.string.empty_no_tide_data),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tideEvents, key = { "${it.time}_${it.type}" }) { event ->
                    TideEventCard(event = event)
                }
            }
        }
    }
}

@Composable
private fun TideEventCard(event: TideEventUiData) {
    val colors = StitchTheme.colors
    val isHigh = event.type == TideType.HIGH
    val typeLabel = if (isHigh) {
        stringResource(R.string.trends_high_tide)
    } else {
        stringResource(R.string.trends_low_tide)
    }
    val typeColor = if (isHigh) StitchPrimary else StitchAmber
    val contentColor = if (event.isNext) MaterialTheme.colorScheme.primary else colors.textSecondary

    val cardContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .width(100.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (event.isNext) {
                Text(
                    text = stringResource(R.string.trends_next_tide),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            Text(
                text = event.timeFormatted,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )

            Icon(
                imageVector = Icons.Default.Waves,
                contentDescription = stringResource(R.string.cd_tide_icon),
                tint = typeColor,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = typeLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = typeColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Text(
                text = event.height,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
        }
    }

    if (event.isNext) {
        ActiveCard(
            modifier = Modifier.width(100.dp),
            cornerRadius = 12.dp
        ) {
            cardContent()
        }
    } else {
        GlassCard(
            modifier = Modifier.width(100.dp),
            cornerRadius = 12.dp
        ) {
            cardContent()
        }
    }
}

@Composable
private fun WindDirectionSection(hourly: List<HourlyForecastUiData>) {
    val colors = StitchTheme.colors
    Column {
        StitchSectionHeader(
            title = androidx.compose.ui.res.stringResource(id = R.string.trends_wind_direction),
            actionText = androidx.compose.ui.res.stringResource(id = R.string.trends_intervals),
            onActionClick = {}
        )

        if (hourly.isEmpty()) {
            Text(
                text = androidx.compose.ui.res.stringResource(id = R.string.empty_no_chart_data),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(hourly, key = { it.time }) { entry ->
                    WindDirectionCard(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun WindDirectionCard(entry: HourlyForecastUiData) {
    val context = LocalContext.current
    val colors = StitchTheme.colors
    val directionLabel = getDirectionLabel(context, entry.windDirection)
    val contentColor = if (entry.isNow) MaterialTheme.colorScheme.primary else colors.textSecondary

    val cardContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .width(80.dp)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${entry.windDirection}°",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = directionLabel,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                contentDescription = stringResource(R.string.cd_wind_direction_chart),
                tint = contentColor,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(entry.windDirection.toFloat())
            )
            Text(
                text = "${entry.windSpeed.toInt()} kt",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }
    }

    if (entry.isNow) {
        ActiveCard(
            modifier = Modifier.width(80.dp),
            cornerRadius = 12.dp
        ) {
            cardContent()
        }
    } else {
        GlassCard(
            modifier = Modifier.width(80.dp),
            cornerRadius = 12.dp
        ) {
            cardContent()
        }
    }
}

@Composable
private fun AttributionFooter() {
    val colors = StitchTheme.colors
    Text(
        text = androidx.compose.ui.res.stringResource(id = R.string.trends_data_attribution).uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = colors.textSecondary,
        textAlign = TextAlign.Center,
        letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    )
}

@Composable
private fun TrendsLoadingSkeleton(modifier: Modifier = Modifier) {
    val colors = StitchTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "trends_shimmer")
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header skeleton
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .width(220.dp)
                    .height(28.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(colors.glassBackground.copy(alpha = alpha))
            )
        }

        // Chart skeletons
        items(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(220.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(colors.glassBackground.copy(alpha = alpha))
            )
        }
    }
}

private fun getDirectionLabel(context: android.content.Context, degrees: Int): String {
    return when (((degrees + 22) / 45) % 8) {
        0 -> context.getString(R.string.direction_n)
        1 -> context.getString(R.string.direction_ne)
        2 -> context.getString(R.string.direction_e)
        3 -> context.getString(R.string.direction_se)
        4 -> context.getString(R.string.direction_s)
        5 -> context.getString(R.string.direction_sw)
        6 -> context.getString(R.string.direction_w)
        7 -> context.getString(R.string.direction_nw)
        else -> context.getString(R.string.direction_n)
    }
}
