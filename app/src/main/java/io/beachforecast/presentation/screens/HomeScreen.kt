package io.beachforecast.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import io.beachforecast.widget.WidgetUpdateHelper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.beachforecast.R
import io.beachforecast.data.location.LocationProvider
import io.beachforecast.data.preferences.UserPreferences
import io.beachforecast.data.repository.BeachRepository
import io.beachforecast.domain.usecases.FindClosestBeachUseCase
import io.beachforecast.domain.calculators.ActivityRecommendationCalculator
import io.beachforecast.domain.calculators.BestDayCalculator
import io.beachforecast.domain.models.Activity
import io.beachforecast.domain.models.BeachSelection
import io.beachforecast.domain.models.CurrentConditions
import io.beachforecast.domain.models.UnitSystem
import io.beachforecast.presentation.components.BeachPickerDialog
import io.beachforecast.presentation.components.BestForCard
import io.beachforecast.presentation.components.HourlyConditionData
import io.beachforecast.presentation.components.LiveVitalsGrid
import io.beachforecast.presentation.components.LocationHeader
import io.beachforecast.presentation.components.TodaysConditionsRow
import io.beachforecast.presentation.components.WeeklyBestDayCard
import io.beachforecast.presentation.components.VitalData
import io.beachforecast.presentation.models.WeatherUiState
import io.beachforecast.ui.theme.StitchAmber
import io.beachforecast.ui.theme.StitchTheme
import kotlinx.coroutines.launch

/**
 * Home screen showing current conditions and today's forecast
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: WeatherUiState,
    onRefresh: () -> Unit,
    onViewChartClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isRefreshing = uiState is WeatherUiState.Loading

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        when (uiState) {
            is WeatherUiState.Initial, is WeatherUiState.Loading -> {
                LoadingSkeleton()
            }

            is WeatherUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                            val haptic = LocalHapticFeedback.current
                            
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onRefresh()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.animateContentSize()
                            ) {
                                val context = LocalContext.current
                                Icon(Icons.Default.Refresh, contentDescription = context.getString(R.string.home_refresh))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(context.getString(R.string.home_try_again))
                            }
                        }
                    }
                }
            }

            is WeatherUiState.Success -> {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)) + 
                            slideInVertically(
                                initialOffsetY = { 20 },
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    HomeContent(
                        uiState = uiState,
                        onRefresh = onRefresh,
                        onViewChartClick = onViewChartClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: WeatherUiState.Success,
    onRefresh: () -> Unit,
    onViewChartClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val userPreferences = remember { UserPreferences(context) }

    // Beach picker state
    val beachRepository = remember { BeachRepository(context) }
    val allBeaches = remember { beachRepository.getAllBeaches() }
    val beachSelection by userPreferences.beachSelectionFlow.collectAsState(initial = BeachSelection.Auto)
    val selectedLanguage by userPreferences.appLanguageFlow.collectAsState(initial = "en")

    // Resolve nearest beach for picker display
    val locationProvider = remember { LocationProvider(context) }
    val findClosestBeach = remember { FindClosestBeachUseCase() }
    var nearestBeachId by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(allBeaches) {
        if (allBeaches.isNotEmpty()) {
            val locationResult = locationProvider.getLocation()
            if (locationResult is io.beachforecast.domain.models.WeatherResult.Success) {
                val loc = locationResult.data
                nearestBeachId = findClosestBeach.execute(loc.latitude, loc.longitude, allBeaches).id
            }
        }
    }
    val unitSystem by userPreferences.unitSystemFlow.collectAsState(initial = UnitSystem.METRIC)
    val selectedSports by userPreferences.selectedSportsFlow.collectAsState(initial = Activity.getDefaults())
    var showBeachDialog by rememberSaveable { mutableStateOf(false) }

    // Helper function to get localized time ago string
    fun getTimeAgoString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = (diff / 60000).toInt()

        return when {
            minutes < 1 -> context.getString(R.string.home_just_now)
            minutes < 60 -> context.getString(R.string.home_minutes_ago, minutes)
            else -> {
                val hours = minutes / 60
                if (hours < 24) context.getString(R.string.home_hours_ago, hours)
                else context.getString(R.string.home_days_ago, hours / 24)
            }
        }
    }

    // Calculate activity recommendations from current conditions
    val currentConditions = remember(uiState.data) {
        CurrentConditions(
            waveCategory = uiState.data.current.waveCategory,
            waveHeightMeters = uiState.data.current.waveHeight,
            temperatureCelsius = uiState.data.current.temperature,
            cloudCover = uiState.data.current.cloudCover,
            windSpeedKmh = uiState.data.current.windSpeed,
            windDirectionDegrees = uiState.data.current.windDirection,
            waveDirectionDegrees = uiState.data.current.waveDirection,
            wavePeriodSeconds = uiState.data.current.wavePeriod,
            swellHeightMeters = uiState.data.current.swellHeight,
            swellDirectionDegrees = uiState.data.current.swellDirection,
            swellPeriodSeconds = uiState.data.current.swellPeriod,
            seaSurfaceTemperatureCelsius = uiState.data.current.seaSurfaceTemperature,
            uvIndex = uiState.data.current.uvIndex
        )
    }
    val recommendations = remember(currentConditions, selectedSports) {
        ActivityRecommendationCalculator.calculateForSports(currentConditions, selectedSports)
    }

    // Build hourly condition data for horizontal scroll
    val hourlyData = remember(uiState.data.todayPeriods) {
        uiState.data.todayPeriods.mapIndexed { index, period ->
            HourlyConditionData(
                time = period.timeLabel,
                waveHeightRange = period.waveHeightFormatted,
                isNow = period.isNow,
                weatherIcon = when {
                    period.cloudCover.ordinal <= 1 -> Icons.Default.WbSunny
                    else -> Icons.Default.Cloud
                }
            )
        }
    }

    // Build vitals data
    val vitalsData = remember(uiState.data.current, unitSystem) {
        buildList {
            // Wind
            if (uiState.data.current.windSpeed > 0) {
                add(VitalData(
                    label = context.getString(R.string.vital_wind),
                    value = uiState.data.current.windSpeedFormatted,
                    icon = Icons.Default.Air,
                    secondaryIcon = Icons.Default.NorthWest
                ))
            }

            // Swell
            if (uiState.data.current.swellHeight > 0) {
                add(VitalData(
                    label = context.getString(R.string.vital_swell),
                    value = "${uiState.data.current.swellHeightFormatted} ${uiState.data.current.swellPeriodFormatted}",
                    icon = Icons.Default.Waves
                ))
            }

            // Avg Wave Period
            if (uiState.data.current.wavePeriod > 0) {
                add(VitalData(
                    label = context.getString(R.string.vital_avg_wave_period),
                    value = uiState.data.current.wavePeriodFormatted,
                    icon = Icons.Default.Timer
                ))
            }

            // Sea Temp
            if (uiState.data.current.seaSurfaceTemperature > 0) {
                add(VitalData(
                    label = context.getString(R.string.vital_sea_temp),
                    value = uiState.data.current.seaSurfaceTemperatureFormatted,
                    icon = Icons.Default.Thermostat
                ))
            }

            // UV Index
            if (uiState.data.current.uvIndex > 0) {
                add(VitalData(
                    label = context.getString(R.string.vital_uv_index),
                    value = "${uiState.data.current.uvIndex.toInt()} ${getUvLevelText(context, uiState.data.current.uvIndex)}",
                    icon = Icons.Default.WbSunny,
                    accentColor = StitchAmber
                ))
            }
        }
    }

    if (showBeachDialog) {
        BeachPickerDialog(
            currentSelection = beachSelection,
            beaches = allBeaches,
            nearestBeachId = nearestBeachId,
            languageCode = selectedLanguage,
            onBeachSelected = { selection ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                    userPreferences.setBeachSelection(selection)
                    // Update widgets
                    WidgetUpdateHelper.enqueueUpdate(context)
                }
                showBeachDialog = false
            },
            onDismiss = { showBeachDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location header with live status
        item {
            LocationHeader(
                beachName = uiState.data.location.cityName,
                lastUpdatedText = getTimeAgoString(uiState.data.lastUpdated),
                onLocationClick = { showBeachDialog = true }
            )
        }

        // Best For card with activity recommendations
        item {
            BestForCard(
                recommendations = recommendations,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Best days this week card
        item {
            val bestDays = remember(uiState.data.weekForecast, selectedSports) {
                BestDayCalculator.findBestDayPerSport(uiState.data.weekForecast, selectedSports)
            }
            WeeklyBestDayCard(
                bestDays = bestDays,
                weekForecast = uiState.data.weekForecast
            )
        }

        // Today's Conditions - horizontal hourly cards
        item {
            TodaysConditionsRow(
                hourlyData = hourlyData,
                onViewChartClick = onViewChartClick
            )
        }

        // Live Vitals - 2x2 grid
        item {
            LiveVitalsGrid(vitals = vitalsData)
        }

        // Bottom padding for navigation bar
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}


@Composable
private fun LoadingSkeleton() {
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

    val stitchColors = StitchTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location header skeleton
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(stitchColors.glassBackground)
                )
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(stitchColors.glassBackground)
                )
            }
        }

        // Best For card skeleton
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(stitchColors.glassBackground)
            )
        }

        // Today's Conditions section skeleton
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(stitchColors.glassBackground)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(stitchColors.glassBackground)
                        )
                    }
                }
            }
        }

        // Live Vitals grid skeleton
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(stitchColors.glassBackground)
                )

                repeat(2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(stitchColors.glassBackground)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getUvLevelText(context: android.content.Context, uvIndex: Double): String {
    return when {
        uvIndex < 3 -> context.getString(R.string.uv_low)
        uvIndex < 6 -> context.getString(R.string.uv_moderate)
        uvIndex < 8 -> context.getString(R.string.uv_high)
        uvIndex < 11 -> context.getString(R.string.uv_very_high)
        else -> context.getString(R.string.uv_extreme)
    }
}
