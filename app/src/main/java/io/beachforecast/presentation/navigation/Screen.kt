package io.beachforecast.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import io.beachforecast.R

/**
 * Navigation destinations - Stitch design system
 * Home, Forecast, Trends, Settings
 */
sealed class Screen(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    object Home : Screen(
        route = "home",
        titleRes = R.string.nav_home,
        icon = Icons.Default.Home
    )

    object Forecast : Screen(
        route = "forecast",
        titleRes = R.string.nav_forecast,
        icon = Icons.Default.DateRange
    )

    object Trends : Screen(
        route = "trends",
        titleRes = R.string.nav_trends,
        icon = Icons.Default.ShowChart
    )

    object Settings : Screen(
        route = "settings",
        titleRes = R.string.nav_settings,
        icon = Icons.Default.Settings
    )

    object Onboarding : Screen(
        route = "onboarding",
        titleRes = R.string.onboarding_title,
        icon = Icons.Default.Check
    )

}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Forecast,
    Screen.Trends,
    Screen.Settings
)
