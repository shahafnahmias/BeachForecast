package io.beachforecast.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import android.content.Context
import androidx.activity.ComponentActivity
import com.google.firebase.analytics.FirebaseAnalytics
import io.beachforecast.BuildConfig
import io.beachforecast.R
import io.beachforecast.widget.WidgetUpdateHelper
import io.beachforecast.data.preferences.UserPreferences
import io.beachforecast.domain.models.Activity
import io.beachforecast.domain.models.AppTheme
import io.beachforecast.domain.models.UnitSystem
import io.beachforecast.domain.models.WeatherMetric

import kotlinx.coroutines.launch

private fun triggerWidgetUpdate(context: Context) {
    WidgetUpdateHelper.enqueueUpdate(context)
}

/**
 * Settings screen
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val unitSystem by userPreferences.unitSystemFlow.collectAsState(initial = UnitSystem.METRIC)
    val selectedTheme by userPreferences.appThemeFlow.collectAsState(initial = AppTheme.SYSTEM)
    val selectedMetrics by userPreferences.selectedMetricsFlow.collectAsState(initial = WeatherMetric.getDefaults())
    val selectedSports by userPreferences.selectedSportsFlow.collectAsState(initial = Activity.getDefaults())
    val selectedLanguage by userPreferences.appLanguageFlow.collectAsState(initial = "en")
    val analyticsEnabled by userPreferences.analyticsEnabledFlow.collectAsState(initial = true)
    val useMetric = unitSystem == UnitSystem.METRIC
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showMetricsDialog by rememberSaveable { mutableStateOf(false) }
    var showSportsDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacyPolicyDialog by rememberSaveable { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = context.getString(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Sports of interest section
        item {
            SectionHeader(title = context.getString(R.string.settings_sports_of_interest))
        }

        item {
            val sportNames = Activity.all
                .filter { selectedSports.contains(it) }
                .joinToString(", ") { context.getString(it.nameRes) }
            SettingsItem(
                title = context.getString(R.string.settings_sports_of_interest),
                subtitle = context.getString(R.string.settings_sports_selected, selectedSports.size, sportNames),
                icon = Icons.Default.Surfing,
                onClick = { showSportsDialog = true }
            )
        }

        // Units section
        item {
            SectionHeader(title = context.getString(R.string.settings_units))
        }

        item {
            SettingsSwitch(
                title = context.getString(R.string.settings_use_metric),
                subtitle = if (useMetric) context.getString(R.string.settings_metric_subtitle) else context.getString(R.string.settings_imperial_subtitle),
                icon = Icons.Default.Straighten,
                checked = useMetric,
                onCheckedChange = { newValue ->
                    scope.launch {
                        userPreferences.setUseMetric(newValue)
                    }
                }
            )
        }

        // Display preferences section
        item {
            SectionHeader(title = context.getString(R.string.settings_display_preferences))
        }

        item {
            val metricsNames = selectedMetrics.joinToString(", ") { context.getString(it.nameRes) }
            SettingsItem(
                title = context.getString(R.string.settings_important_metrics),
                subtitle = context.getString(R.string.settings_metrics_selected, selectedMetrics.size, metricsNames),
                icon = Icons.Default.Menu,
                onClick = { showMetricsDialog = true }
            )
        }

        // Appearance section
        item {
            SectionHeader(title = context.getString(R.string.settings_appearance))
        }

        item {
            SettingsItem(
                title = context.getString(R.string.settings_theme),
                subtitle = context.getString(selectedTheme.nameRes),
                icon = Icons.Default.Palette,
                onClick = { showThemeDialog = true }
            )
        }

        item {
            val languageDisplayName = when (selectedLanguage) {
                "he" -> context.getString(R.string.language_hebrew)
                else -> context.getString(R.string.language_english)
            }
            SettingsItem(
                title = context.getString(R.string.settings_language),
                subtitle = languageDisplayName,
                icon = Icons.Default.Language,
                onClick = { showLanguageDialog = true }
            )
        }

        // About section
        item {
            SectionHeader(title = context.getString(R.string.settings_about))
        }

        item {
            SettingsItem(
                title = context.getString(R.string.settings_app_version),
                subtitle = BuildConfig.VERSION_NAME,
                icon = Icons.Default.Info,
                onClick = { }
            )
        }

        item {
            SettingsItem(
                title = context.getString(R.string.settings_data_source),
                subtitle = context.getString(R.string.settings_data_source_subtitle),
                icon = Icons.Default.Info,
                onClick = { }
            )
        }

        item {
            SettingsSwitch(
                title = context.getString(R.string.settings_analytics_title),
                subtitle = if (analyticsEnabled) {
                    context.getString(R.string.settings_analytics_subtitle_on)
                } else {
                    context.getString(R.string.settings_analytics_subtitle_off)
                },
                icon = Icons.Default.BarChart,
                checked = analyticsEnabled,
                onCheckedChange = { newValue ->
                    scope.launch {
                        userPreferences.setAnalyticsEnabled(newValue)
                        // Apply immediately
                        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(newValue)
                    }
                }
            )
        }

        item {
            SettingsItem(
                title = context.getString(R.string.settings_privacy_policy),
                subtitle = context.getString(R.string.settings_privacy_policy_subtitle),
                icon = Icons.Default.Shield,
                onClick = { showPrivacyPolicyDialog = true }
            )
        }
    }

    // Sports picker dialog
    if (showSportsDialog) {
        SportsPickerDialog(
            selectedSports = selectedSports,
            onSportsSelected = { sports ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                    userPreferences.setSelectedSports(sports)
                    triggerWidgetUpdate(context)
                }
                showSportsDialog = false
            },
            onDismiss = { showSportsDialog = false }
        )
    }

    // Theme picker dialog
    if (showThemeDialog) {
        ThemePickerDialog(
            currentTheme = selectedTheme,
            onThemeSelected = { theme ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                    userPreferences.setAppTheme(theme)
                    triggerWidgetUpdate(context)
                }
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Metrics picker dialog
    if (showMetricsDialog) {
        MetricsPickerDialog(
            selectedMetrics = selectedMetrics,
            onMetricsSelected = { metrics ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                    userPreferences.setSelectedMetrics(metrics)
                    triggerWidgetUpdate(context)
                }
                showMetricsDialog = false
            },
            onDismiss = { showMetricsDialog = false }
        )
    }

    // Language picker dialog
    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentLanguage = selectedLanguage,
            onLanguageSelected = { language ->
                scope.launch {
                    userPreferences.setAppLanguage(language)
                    triggerWidgetUpdate(context)

                    // Restart the app to fully apply language changes
                    val activity = context as? ComponentActivity
                    activity?.let {
                        val intent = it.intent
                        it.finish()
                        it.startActivity(intent)
                    }
                }
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    // Privacy policy dialog
    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyPolicyDialog = false }
        )
    }
}

@Composable
private fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.privacy_policy_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 400.dp)
                ) {
                    item {
                        Text(
                            text = context.getString(R.string.privacy_policy_full_text),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(context.getString(R.string.privacy_policy_close))
                }
            }
        }
    }
}

@Composable
private fun ThemePickerDialog(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.theme_picker_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                AppTheme.entries.forEach { theme ->
                    ThemeOption(
                        theme = theme,
                        isSelected = theme == currentTheme,
                        onClick = { onThemeSelected(theme) }
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(context.getString(R.string.theme_cancel))
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = context.getString(theme.nameRes),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = getThemeDescription(theme, context),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = context.getString(R.string.cd_selected),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun getThemeDescription(theme: AppTheme, context: android.content.Context): String {
    return when (theme) {
        AppTheme.SYSTEM -> context.getString(R.string.theme_system_description)
        AppTheme.DARK -> context.getString(R.string.theme_dark_description)
        AppTheme.LIGHT -> context.getString(R.string.theme_light_description)
    }
}

@Composable
private fun LanguagePickerDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.language_picker_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LanguageOption(
                    languageCode = "en",
                    languageName = context.getString(R.string.language_english),
                    isSelected = currentLanguage == "en",
                    onClick = { onLanguageSelected("en") }
                )

                LanguageOption(
                    languageCode = "he",
                    languageName = context.getString(R.string.language_hebrew),
                    isSelected = currentLanguage == "he",
                    onClick = { onLanguageSelected("he") }
                )

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(context.getString(R.string.language_cancel))
                }
            }
        }
    }
}

@Composable
private fun LanguageOption(
    languageCode: String,
    languageName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = languageName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = context.getString(R.string.cd_selected),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun MetricsPickerDialog(
    selectedMetrics: Set<WeatherMetric>,
    onMetricsSelected: (Set<WeatherMetric>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var tempSelectedMetrics by remember { mutableStateOf(selectedMetrics) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = context.getString(R.string.metrics_picker_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = context.getString(R.string.metrics_picker_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                WeatherMetric.getAll().forEach { metric ->
                    MetricOption(
                        metric = metric,
                        isSelected = tempSelectedMetrics.contains(metric),
                        onToggle = {
                            tempSelectedMetrics = if (tempSelectedMetrics.contains(metric)) {
                                tempSelectedMetrics - metric
                            } else {
                                tempSelectedMetrics + metric
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(context.getString(R.string.metrics_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onMetricsSelected(tempSelectedMetrics) },
                        enabled = tempSelectedMetrics.isNotEmpty()
                    ) {
                        Text(context.getString(R.string.metrics_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun SportsPickerDialog(
    selectedSports: Set<Activity>,
    onSportsSelected: (Set<Activity>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var tempSelectedSports by remember { mutableStateOf(selectedSports) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = context.getString(R.string.sports_picker_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = context.getString(R.string.sports_picker_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Activity.all.forEach { activity ->
                    SportOption(
                        activity = activity,
                        isSelected = tempSelectedSports.contains(activity),
                        onToggle = {
                            tempSelectedSports = if (tempSelectedSports.contains(activity)) {
                                tempSelectedSports - activity
                            } else {
                                tempSelectedSports + activity
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(context.getString(R.string.sports_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSportsSelected(tempSelectedSports) },
                        enabled = tempSelectedSports.isNotEmpty()
                    ) {
                        Text(context.getString(R.string.sports_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun SportOption(
    activity: Activity,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = context.getString(activity.nameRes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = context.getString(R.string.cd_selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricOption(
    metric: WeatherMetric,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = context.getString(metric.nameRes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = context.getString(R.string.cd_selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.3.sp,
        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = context.getString(R.string.cd_navigate_to, title),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )
            }

            val haptic = LocalHapticFeedback.current

            Switch(
                checked = checked,
                onCheckedChange = { newValue ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCheckedChange(newValue)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }
    }
}
