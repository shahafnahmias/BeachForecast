package io.beachforecast.presentation.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Surfing
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.beachforecast.R
import io.beachforecast.data.location.LocationProvider
import io.beachforecast.data.preferences.UserPreferences
import io.beachforecast.data.repository.BeachRepository
import io.beachforecast.domain.models.Activity
import io.beachforecast.domain.models.BeachSelection
import io.beachforecast.domain.models.WeatherResult
import io.beachforecast.domain.usecases.FindClosestBeachUseCase
import io.beachforecast.presentation.components.BeachOption
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val beachRepository = remember { BeachRepository(context) }
    val allBeaches = remember { beachRepository.getAllBeaches() }
    val selectedLanguage by userPreferences.appLanguageFlow.collectAsState(initial = "en")
    val scope = rememberCoroutineScope()

    var currentStep by remember { mutableIntStateOf(0) }
    var selectedBeach by remember { mutableStateOf<BeachSelection>(BeachSelection.Auto) }
    var selectedSports by remember { mutableStateOf(Activity.getDefaults()) }

    // Nearest beach resolution
    val locationProvider = remember { LocationProvider(context) }
    val findClosestBeach = remember { FindClosestBeachUseCase() }
    var nearestBeachId by remember { mutableStateOf<String?>(null) }

    // Location permission launcher for step 0
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        if (granted && allBeaches.isNotEmpty()) {
            scope.launch {
                val locationResult = locationProvider.getLocation()
                if (locationResult is WeatherResult.Success) {
                    val loc = locationResult.data
                    nearestBeachId = findClosestBeach.execute(loc.latitude, loc.longitude, allBeaches).id
                }
                currentStep = 1
            }
        } else {
            // No location permission — advance without nearest indication
            currentStep = 1
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        // Top bar with back button and step indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                IconButton(
                    onClick = { currentStep-- }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.onboarding_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Step indicator dots
            StepIndicator(
                totalSteps = 3,
                currentStep = currentStep
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Animated step content
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()) togetherWith
                            (slideOutHorizontally { it } + fadeOut())
                }
            },
            modifier = Modifier.weight(1f),
            label = "onboarding_step"
        ) { step ->
            when (step) {
                0 -> WelcomeStep(
                    onGetStarted = {
                        locationLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
                1 -> BeachSelectionStep(
                    allBeaches = allBeaches,
                    nearestBeachId = nearestBeachId,
                    languageCode = selectedLanguage,
                    selectedBeach = selectedBeach,
                    onBeachSelected = { selectedBeach = it },
                    onNext = { currentStep = 2 }
                )
                2 -> SportSelectionStep(
                    selectedSports = selectedSports,
                    onSportsChanged = { selectedSports = it },
                    onFinish = {
                        scope.launch {
                            userPreferences.setBeachSelection(selectedBeach)
                            userPreferences.setSelectedSports(selectedSports)
                            userPreferences.setOnboardingCompleted(true)
                            onComplete()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(
    totalSteps: Int,
    currentStep: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentStep) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index <= currentStep) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                    )
            )
        }
    }
}

// Step 0: Welcome + Location Permission
@Composable
private fun WelcomeStep(
    onGetStarted: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(stringResource(R.string.onboarding_get_started))
        }
    }
}

// Step 1: Beach Selection
@Composable
private fun BeachSelectionStep(
    allBeaches: List<io.beachforecast.domain.models.Beach>,
    nearestBeachId: String?,
    languageCode: String,
    selectedBeach: BeachSelection,
    onBeachSelected: (BeachSelection) -> Unit,
    onNext: () -> Unit
) {
    // Resolve the effective selected beach ID for display
    val selectedBeachId = when (selectedBeach) {
        is BeachSelection.Auto -> nearestBeachId
        is BeachSelection.Manual -> selectedBeach.beachId
    }

    // Sort beaches: nearest first, then the rest in original order
    val sortedBeaches = remember(allBeaches, nearestBeachId) {
        if (nearestBeachId != null) {
            val nearest = allBeaches.find { it.id == nearestBeachId }
            val rest = allBeaches.filter { it.id != nearestBeachId }
            if (nearest != null) listOf(nearest) + rest else allBeaches
        } else {
            allBeaches
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.onboarding_select_beach),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_select_beach_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedBeaches, key = { it.id }) { beach ->
                val isNearest = beach.id == nearestBeachId
                BeachOption(
                    title = beach.getLocalizedName(languageCode),
                    subtitle = if (isNearest) stringResource(R.string.beach_nearest_to_you) else null,
                    isSelected = beach.id == selectedBeachId,
                    onClick = {
                        val selection = if (isNearest) {
                            BeachSelection.Auto
                        } else {
                            BeachSelection.Manual(beach.id)
                        }
                        onBeachSelected(selection)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.onboarding_next))
        }
    }
}

// Step 2: Sport Selection
@Composable
private fun SportSelectionStep(
    selectedSports: Set<Activity>,
    onSportsChanged: (Set<Activity>) -> Unit,
    onFinish: () -> Unit
) {
    val canFinish = selectedSports.isNotEmpty()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.onboarding_select_sports),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Activity.all.forEach { activity ->
                SportSelectionCard(
                    activity = activity,
                    isSelected = selectedSports.contains(activity),
                    onToggle = {
                        onSportsChanged(
                            if (selectedSports.contains(activity)) {
                                selectedSports - activity
                            } else {
                                selectedSports + activity
                            }
                        )
                    }
                )
            }

            if (!canFinish) {
                Text(
                    text = stringResource(R.string.onboarding_at_least_one),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onFinish,
            enabled = canFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.onboarding_finish))
        }
    }
}

@Composable
private fun SportSelectionCard(
    activity: Activity,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val icon = when (activity) {
        Activity.SWIM -> Icons.Default.Pool
        Activity.SURF -> Icons.Default.Surfing
        Activity.KITE -> Icons.Default.Air
        Activity.SUP -> Icons.Default.Surfing
    }

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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.cd_sport_icon, stringResource(activity.nameRes)),
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = stringResource(activity.nameRes),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.cd_selected),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
