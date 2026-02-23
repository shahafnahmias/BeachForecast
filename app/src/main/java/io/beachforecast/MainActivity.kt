package io.beachforecast

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.beachforecast.data.preferences.UserPreferences
import io.beachforecast.domain.models.AppTheme
import io.beachforecast.presentation.navigation.Screen
import io.beachforecast.presentation.navigation.bottomNavItems
import io.beachforecast.presentation.screens.*
import io.beachforecast.presentation.viewmodels.WeatherViewModel
import io.beachforecast.presentation.viewmodels.WeatherViewModelFactory
import io.beachforecast.ui.theme.SeaLevelWidgetTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel by viewModels {
        WeatherViewModelFactory(applicationContext)
    }

    private var showLocationRationale = mutableStateOf(false)
    private var showLocationSettingsDialog = mutableStateOf(false)
    private var onboardingCompleted = false

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                viewModel.loadWeatherData()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                viewModel.loadWeatherData()
            }
            else -> {
                // Permission denied — check if permanently denied
                val shouldShowFine = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                )
                val shouldShowCoarse = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (!shouldShowFine && !shouldShowCoarse) {
                    showLocationSettingsDialog.value = true
                }
            }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun hasRequestedLocationBefore(): Boolean {
        val prefs = getSharedPreferences("permission_prefs", MODE_PRIVATE)
        return prefs.getBoolean("location_requested", false)
    }

    private fun markLocationRequested() {
        getSharedPreferences("permission_prefs", MODE_PRIVATE)
            .edit().putBoolean("location_requested", true).apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request location permissions with rationale
        val hasFine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Request location permissions if not granted (onboarding handles initial request, this handles re-request after revocation)
        if (!hasFine && !hasCoarse) {
            val shouldShowFine = ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            )
            val shouldShowCoarse = ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            )

            when {
                shouldShowFine || shouldShowCoarse -> {
                    showLocationRationale.value = true
                }
                hasRequestedLocationBefore() -> {
                    showLocationSettingsDialog.value = true
                }
                else -> {
                    markLocationRequested()
                    requestLocationPermission()
                }
            }
        }

        setContent {
            val userPreferences = remember { UserPreferences(applicationContext) }
            val selectedTheme by userPreferences.appThemeFlow.collectAsState(initial = AppTheme.SYSTEM)

            // Get current locale from system (already set in attachBaseContext)
            val currentLocale = resources.configuration.locales[0]

            val layoutDirection = if (currentLocale.language == "he" || currentLocale.language == "iw") {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                SeaLevelWidgetTheme(appTheme = selectedTheme) {
                    SeaLevelApp(
                        viewModel = viewModel,
                        onboardingCompleted = onboardingCompleted
                    )

                    // Location permission rationale dialog
                    if (showLocationRationale.value) {
                        AlertDialog(
                            onDismissRequest = { showLocationRationale.value = false },
                            title = { Text(getString(R.string.location_rationale_title)) },
                            text = { Text(getString(R.string.location_rationale_message)) },
                            confirmButton = {
                                TextButton(onClick = {
                                    showLocationRationale.value = false
                                    requestLocationPermission()
                                }) {
                                    Text(getString(R.string.location_rationale_allow))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showLocationRationale.value = false
                                }) {
                                    Text(getString(R.string.location_rationale_deny))
                                }
                            }
                        )
                    }

                    // Location settings dialog (permanent denial)
                    if (showLocationSettingsDialog.value) {
                        AlertDialog(
                            onDismissRequest = { showLocationSettingsDialog.value = false },
                            title = { Text(getString(R.string.location_settings_title)) },
                            text = { Text(getString(R.string.location_settings_message)) },
                            confirmButton = {
                                TextButton(onClick = {
                                    showLocationSettingsDialog.value = false
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", packageName, null)
                                    )
                                    startActivity(intent)
                                }) {
                                    Text(getString(R.string.location_settings_open))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showLocationSettingsDialog.value = false
                                }) {
                                    Text(getString(R.string.location_settings_cancel))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val userPreferences = UserPreferences(newBase)
        val languageCode = userPreferences.getAppLanguageSync()
        onboardingCompleted = userPreferences.isOnboardingCompletedSync()
        val localizedContext = updateBaseContextLocale(newBase, languageCode)
        super.attachBaseContext(localizedContext)
    }

    private fun updateBaseContextLocale(context: Context, languageCode: String): Context {
        val locale = if (languageCode == "he") {
            Locale("he")  // Hebrew
        } else {
            Locale("en")  // English
        }
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeaLevelApp(
    viewModel: WeatherViewModel,
    onboardingCompleted: Boolean
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute != Screen.Onboarding.route) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    val currentDestination = navBackStackEntry?.destination

                    bottomNavItems.forEach { screen ->
                        val title = context.getString(screen.titleRes)
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = title
                                )
                            },
                            label = { Text(title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingCompleted) Screen.Home.route else Screen.Onboarding.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    uiState = uiState,
                    onRefresh = { viewModel.refresh() },
                    onViewChartClick = {
                        navController.navigate(Screen.Trends.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Forecast.route) {
                ForecastScreen(uiState = uiState)
            }

            composable(Screen.Trends.route) {
                TrendsScreen(uiState = uiState)
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
