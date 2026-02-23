# Glance Widget Rewrite Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Rewrite all 3 Beach Forecast widgets from RemoteViews to Jetpack Glance with Material You dynamic colors, each serving a distinct purpose.

**Architecture:** Glance Compose widgets backed by a shared DataStore state written by WidgetUpdateWorker. Pre-computed display data (activity recommendations, daily averages) serialized as JSON. GlanceTheme with dynamic colors on Android 12+, Stitch palette fallback on older devices.

**Tech Stack:** Jetpack Glance 1.1.1, Glance Material3, Gson (already in project), WorkManager (already in project), DataStore Preferences (already in project)

**Design doc:** `docs/plans/2026-02-23-glance-widget-rewrite-design.md`

---

### Task 1: Add Glance Dependencies

**Files:**
- Modify: `gradle/libs.versions.toml` — add `glance = "1.1.1"` in `[versions]` and library entries in `[libraries]`
- Modify: `app/build.gradle.kts` — add Glance implementation lines

**Step 1: Add version and library entries to version catalog**

In `gradle/libs.versions.toml`, after `timber = "5.0.1"` (line 25), add:

```toml
glance = "1.1.1"
```

In `[libraries]`, after the `timber` line (line 65), add:

```toml
androidx-glance-appwidget = { group = "androidx.glance", name = "glance-appwidget", version.ref = "glance" }
androidx-glance-material3 = { group = "androidx.glance", name = "glance-material3", version.ref = "glance" }
```

**Step 2: Add dependencies to app build file**

In `app/build.gradle.kts`, after the `implementation(libs.timber)` line (line 124), add:

```kotlin
    // Glance for widget support
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
```

**Step 3: Sync and verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "feat(widget): add Jetpack Glance dependencies"
```

---

### Task 2: Create Widget State Model

**Files:**
- Create: `app/src/main/java/io/beachforecast/widget/WidgetState.kt`

**Step 1: Create the state data classes and serialization helpers**

Create `app/src/main/java/io/beachforecast/widget/WidgetState.kt`:

```kotlin
package io.beachforecast.widget

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Pre-computed widget display data, serialized to DataStore by WidgetUpdateWorker.
 * All formatting/computation happens in the worker; widgets just render.
 */
data class WidgetState(
    val cityName: String = "",
    val conditionRating: String = "",         // ConditionRating enum name
    val conditionRatingDisplay: String = "",  // Localized display text
    val temperatureFormatted: String = "",    // e.g. "25°C"
    val waveHeightFormatted: String = "",     // e.g. "40-60cm"
    val windFormatted: String = "",           // e.g. "12 km/h NW"
    val activities: List<ActivityState> = emptyList(),
    val dayForecasts: List<DayForecastState> = emptyList(),
    val errorMessage: String? = null,
    val lastUpdated: Long = 0L
)

data class ActivityState(
    val name: String,           // Localized activity name
    val isRecommended: Boolean,
    val reason: String          // Localized reason text
)

data class DayForecastState(
    val dayName: String,                // Localized: "Today", "Tomorrow", "Wednesday"
    val conditionRating: String,        // ConditionRating enum name
    val conditionRatingDisplay: String, // Localized display text
    val waveHeightFormatted: String,    // e.g. "40-60cm"
    val windFormatted: String,          // e.g. "12 km/h NW"
    val temperatureFormatted: String    // e.g. "25°C"
)

/**
 * DataStore keys and serialization for widget state.
 */
object WidgetStateSerializer {
    private val gson = Gson()

    // DataStore preference keys
    const val KEY_CITY_NAME = "widget_city_name"
    const val KEY_CONDITION_RATING = "widget_condition_rating"
    const val KEY_CONDITION_RATING_DISPLAY = "widget_condition_rating_display"
    const val KEY_TEMPERATURE = "widget_temperature"
    const val KEY_WAVE_HEIGHT = "widget_wave_height"
    const val KEY_WIND = "widget_wind"
    const val KEY_ACTIVITIES_JSON = "widget_activities_json"
    const val KEY_DAY_FORECASTS_JSON = "widget_day_forecasts_json"
    const val KEY_ERROR_MESSAGE = "widget_error_message"
    const val KEY_LAST_UPDATED = "widget_last_updated"

    fun serializeActivities(activities: List<ActivityState>): String = gson.toJson(activities)

    fun deserializeActivities(json: String): List<ActivityState> {
        if (json.isBlank()) return emptyList()
        val type = object : TypeToken<List<ActivityState>>() {}.type
        return gson.fromJson(json, type)
    }

    fun serializeDayForecasts(forecasts: List<DayForecastState>): String = gson.toJson(forecasts)

    fun deserializeDayForecasts(json: String): List<DayForecastState> {
        if (json.isBlank()) return emptyList()
        val type = object : TypeToken<List<DayForecastState>>() {}.type
        return gson.fromJson(json, type)
    }
}
```

**Step 2: Run tests to verify no compilation issues**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/WidgetState.kt
git commit -m "feat(widget): add WidgetState data model and serializer"
```

---

### Task 3: Create Widget Theme

**Files:**
- Create: `app/src/main/java/io/beachforecast/widget/theme/WidgetTheme.kt`

**Step 1: Create the Glance theme wrapper with condition colors and fallback palette**

Create `app/src/main/java/io/beachforecast/widget/theme/WidgetTheme.kt`:

```kotlin
package io.beachforecast.widget.theme

import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders
import io.beachforecast.domain.models.ConditionRating

/**
 * Stitch fallback colors for pre-Android 12 devices.
 */
private val StitchDarkScheme = darkColorScheme(
    primary = Color(0xFF0DCCF2),
    onPrimary = Color(0xFF101F22),
    surface = Color(0xFF1A2E32),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1A2E32),
    onSurfaceVariant = Color(0xFF90C1CB),
    background = Color(0xFF101F22),
    onBackground = Color(0xFFFFFFFF)
)

private val StitchLightScheme = lightColorScheme(
    primary = Color(0xFF0DCCF2),
    onPrimary = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF101F22),
    surfaceVariant = Color(0xFFF0F7F8),
    onSurfaceVariant = Color(0xFF5A7A82),
    background = Color(0xFFF5F8F8),
    onBackground = Color(0xFF101F22)
)

private val StitchColorProviders = ColorProviders(
    light = StitchLightScheme,
    dark = StitchDarkScheme
)

/**
 * Wraps content in GlanceTheme with dynamic colors on Android 12+
 * and Stitch palette fallback on older devices.
 */
@Composable
fun BeachForecastWidgetTheme(content: @Composable () -> Unit) {
    GlanceTheme(
        colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GlanceTheme.colors // Dynamic colors from wallpaper
        } else {
            StitchColorProviders
        }
    ) {
        content()
    }
}

/**
 * Semantic condition colors — these do NOT change with wallpaper.
 * They carry meaning (good/bad) and must remain consistent.
 */
object ConditionColors {
    fun forRating(ratingName: String): Color = when (ratingName) {
        ConditionRating.EPIC.name -> Color(0xFF00E676)
        ConditionRating.EXCELLENT.name -> Color(0xFF4CAF50)
        ConditionRating.GOOD.name -> Color(0xFF66BB6A)
        ConditionRating.FAIR.name -> Color(0xFFFFB300)
        ConditionRating.POOR.name -> Color(0xFFFF9800)
        ConditionRating.FLAT.name -> Color(0xFF78909C)
        ConditionRating.DANGEROUS.name -> Color(0xFFF44336)
        else -> Color(0xFF78909C)
    }
}
```

**Step 2: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/theme/WidgetTheme.kt
git commit -m "feat(widget): add Glance theme with Material You + Stitch fallback"
```

---

### Task 4: Create Small Widget (Go/No-Go Signal)

**Files:**
- Create: `app/src/main/java/io/beachforecast/widget/SmallWidget.kt`

**Step 1: Create the small widget GlanceAppWidget + Receiver + Composable**

Create `app/src/main/java/io/beachforecast/widget/SmallWidget.kt`:

```kotlin
package io.beachforecast.widget

import android.content.Context
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
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
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

class SmallWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BeachForecastWidgetTheme {
                SmallWidgetContent()
            }
        }
    }
}

class SmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallWidget()
}

@Composable
private fun SmallWidgetContent() {
    val prefs = currentState<Preferences>()
    val cityName = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CITY_NAME)] ?: ""
    val conditionRating = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING)] ?: ""
    val conditionDisplay = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING_DISPLAY)] ?: ""
    val temperature = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_TEMPERATURE)] ?: ""
    val errorMessage = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ERROR_MESSAGE)]
    val lastUpdated = prefs[longPreferencesKey(WidgetStateSerializer.KEY_LAST_UPDATED)] ?: 0L

    val isLoading = lastUpdated == 0L && errorMessage == null
    val hasError = errorMessage != null

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> LoadingContent()
            hasError -> ErrorContent(errorMessage!!)
            else -> GoNoGoContent(cityName, conditionRating, conditionDisplay, temperature)
        }
    }
}

@Composable
private fun GoNoGoContent(
    cityName: String,
    conditionRating: String,
    conditionDisplay: String,
    temperature: String
) {
    val conditionColor = ConditionColors.forRating(conditionRating)

    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header: app icon + beach name
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
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
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Hero: condition rating
        Text(
            text = conditionDisplay,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(conditionColor)
            ),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.size(4.dp))

        // Temperature
        Text(
            text = temperature,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = GlanceTheme.colors.onSurface
            )
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Condition color bar at bottom
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(4.dp)
                .background(conditionColor)
                .cornerRadius(2.dp)
        ) {}
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
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            )
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
            style = TextStyle(
                fontSize = 11.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            ),
            maxLines = 2
        )
    }
}
```

**Step 2: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL (or minor import adjustments needed — Glance API may differ slightly, fix as needed)

**Step 3: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/SmallWidget.kt
git commit -m "feat(widget): add Small widget Glance implementation (go/no-go signal)"
```

---

### Task 5: Create Medium Widget (Activity Recommendations)

**Files:**
- Create: `app/src/main/java/io/beachforecast/widget/MediumWidget.kt`

**Step 1: Create the medium widget with adaptive activity grid**

Create `app/src/main/java/io/beachforecast/widget/MediumWidget.kt`:

```kotlin
package io.beachforecast.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.beachforecast.MainActivity
import io.beachforecast.R
import io.beachforecast.widget.theme.BeachForecastWidgetTheme
import io.beachforecast.widget.theme.ConditionColors

class MediumWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BeachForecastWidgetTheme {
                MediumWidgetContent()
            }
        }
    }
}

class MediumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumWidget()
}

@Composable
private fun MediumWidgetContent() {
    val prefs = currentState<Preferences>()
    val cityName = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CITY_NAME)] ?: ""
    val conditionRating = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING)] ?: ""
    val conditionDisplay = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING_DISPLAY)] ?: ""
    val activitiesJson = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ACTIVITIES_JSON)] ?: ""
    val errorMessage = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ERROR_MESSAGE)]
    val lastUpdated = prefs[longPreferencesKey(WidgetStateSerializer.KEY_LAST_UPDATED)] ?: 0L

    val isLoading = lastUpdated == 0L && errorMessage == null
    val hasError = errorMessage != null
    val activities = WidgetStateSerializer.deserializeActivities(activitiesJson)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        when {
            isLoading -> LoadingContent()
            hasError -> ErrorContent(errorMessage!!)
            else -> ActivityRecommendationsContent(cityName, conditionRating, conditionDisplay, activities)
        }
    }
}

@Composable
private fun ActivityRecommendationsContent(
    cityName: String,
    conditionRating: String,
    conditionDisplay: String,
    activities: List<ActivityState>
) {
    val conditionColor = ConditionColors.forRating(conditionRating)

    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16.dp)
    ) {
        // Header row: icon + beach name + condition badge
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = conditionDisplay,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(conditionColor)
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.size(8.dp))

        // Activity grid — adaptive layout based on count
        when {
            activities.size <= 2 -> ActivityRow(activities)
            activities.size == 3 -> ActivityRow(activities)
            else -> {
                // 2x2 grid
                ActivityRow(activities.take(2))
                Spacer(modifier = GlanceModifier.size(4.dp))
                ActivityRow(activities.drop(2).take(2))
            }
        }
    }
}

@Composable
private fun ActivityRow(activities: List<ActivityState>) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        activities.forEachIndexed { index, activity ->
            if (index > 0) Spacer(modifier = GlanceModifier.size(4.dp))
            ActivityCard(
                activity = activity,
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }
}

@Composable
private fun ActivityCard(activity: ActivityState, modifier: GlanceModifier) {
    val dotColor = if (activity.isRecommended) Color(0xFF4CAF50) else Color(0xFF78909C)

    Column(
        modifier = modifier
            .background(GlanceTheme.colors.surfaceVariant)
            .cornerRadius(12.dp)
            .padding(8.dp)
    ) {
        // Activity name with status dot
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = GlanceModifier
                    .size(8.dp)
                    .background(dotColor)
                    .cornerRadius(4.dp)
            ) {}
            Spacer(modifier = GlanceModifier.size(4.dp))
            Text(
                text = activity.name,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.size(2.dp))

        // Reason
        Text(
            text = activity.reason,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = GlanceTheme.colors.onSurfaceVariant
            ),
            maxLines = 1
        )
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
```

**Step 2: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/MediumWidget.kt
git commit -m "feat(widget): add Medium widget Glance implementation (activity recommendations)"
```

---

### Task 6: Create Large Widget (Multi-Day Outlook)

**Files:**
- Create: `app/src/main/java/io/beachforecast/widget/LargeWidget.kt`

**Step 1: Create the large widget with 3-day forecast rows**

Create `app/src/main/java/io/beachforecast/widget/LargeWidget.kt`:

```kotlin
package io.beachforecast.widget

import android.content.Context
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
            .clickable(actionStartActivity<MainActivity>())
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
                .background(conditionColor)
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
```

**Step 2: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/LargeWidget.kt
git commit -m "feat(widget): add Large widget Glance implementation (multi-day outlook)"
```

---

### Task 7: Rewrite WidgetUpdateWorker for Glance State

**Files:**
- Modify: `app/src/main/java/io/beachforecast/widget/WidgetUpdateWorker.kt`

**Step 1: Rewrite the worker to pre-compute all data and write to DataStore**

Replace the entire contents of `app/src/main/java/io/beachforecast/widget/WidgetUpdateWorker.kt` with:

```kotlin
package io.beachforecast.widget

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.beachforecast.R
import io.beachforecast.data.location.LocationProvider
import io.beachforecast.data.preferences.UserPreferences
import io.beachforecast.data.repository.BeachRepository
import io.beachforecast.data.repository.WeatherRepository
import io.beachforecast.domain.calculators.ActivityRecommendationCalculator
import io.beachforecast.domain.formatters.WeatherFormatter
import io.beachforecast.domain.models.ConditionRating
import io.beachforecast.domain.models.HourlyWaveForecast
import io.beachforecast.domain.models.UnitConverter
import io.beachforecast.domain.models.UnitSystem
import io.beachforecast.domain.models.WaveCategory
import io.beachforecast.domain.models.WeatherResult
import io.beachforecast.domain.usecases.FindClosestBeachUseCase
import io.beachforecast.domain.usecases.GetSelectedBeachUseCase
import io.beachforecast.domain.usecases.GetWidgetDataUseCase
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "widget_update"
    }

    override suspend fun doWork(): Result {
        val userPreferences = UserPreferences(context)
        val languageCode = userPreferences.getAppLanguage()
        val localizedContext = updateContextLocale(context, languageCode)
        val unitSystem = userPreferences.getUnitSystem()
        val selectedSports = userPreferences.getSelectedSports()

        // Initialize dependencies
        val locationProvider = LocationProvider(context)
        val beachRepository = BeachRepository(context)
        val weatherRepository = WeatherRepository()
        val getSelectedBeachUseCase = GetSelectedBeachUseCase(
            beachRepository = beachRepository,
            locationProvider = locationProvider,
            userPreferences = userPreferences,
            findClosestBeachUseCase = FindClosestBeachUseCase()
        )
        val getWidgetDataUseCase = GetWidgetDataUseCase(
            getSelectedBeachUseCase = getSelectedBeachUseCase,
            weatherRepository = weatherRepository,
            languageCode = languageCode
        )

        try {
            val result = getWidgetDataUseCase.execute()

            if (result is WeatherResult.Error) {
                val errorMessage = localizedContext.getString(result.error.getUserMessageRes())
                writeErrorState(errorMessage)
                return Result.retry()
            }

            val widgetData = result.getOrNull() ?: return Result.retry()
            val cityName = widgetData.cityName
            val weatherData = widgetData.weatherData
            val conditions = weatherData.currentConditions

            // Pre-compute activity recommendations
            val recommendations = ActivityRecommendationCalculator.calculateForSports(
                conditions, selectedSports
            )
            val activityStates = recommendations.recommendations.map { rec ->
                ActivityState(
                    name = localizedContext.getString(rec.activity.nameRes),
                    isRecommended = rec.isRecommended,
                    reason = rec.reason ?: ""
                )
            }

            // Pre-compute daily forecasts (today + 2 days)
            val dayForecasts = computeDayForecasts(
                weatherData.weekData, unitSystem, localizedContext, languageCode
            )

            // Write state to DataStore for all widgets
            writeSuccessState(
                cityName = cityName,
                conditionRating = recommendations.conditionRating.name,
                conditionRatingDisplay = localizedContext.getString(recommendations.conditionRating.nameRes),
                temperatureFormatted = WeatherFormatter.formatTemperature(conditions.temperatureCelsius, unitSystem),
                waveHeightFormatted = UnitConverter.formatWaveHeightRange(conditions.waveHeightMeters, unitSystem),
                windFormatted = WeatherFormatter.formatWindCompact(
                    conditions.windSpeedKmh, conditions.windDirectionDegrees, unitSystem
                ),
                activitiesJson = WidgetStateSerializer.serializeActivities(activityStates),
                dayForecastsJson = WidgetStateSerializer.serializeDayForecasts(dayForecasts)
            )

            return Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Widget update failed")
            writeErrorState(localizedContext.getString(R.string.error_unknown))
            return Result.retry()
        }
    }

    private fun computeDayForecasts(
        weekData: List<HourlyWaveForecast>,
        unitSystem: UnitSystem,
        localizedContext: Context,
        languageCode: String
    ): List<DayForecastState> {
        if (weekData.isEmpty()) return emptyList()

        val locale = if (languageCode == "he") Locale("he") else Locale("en")
        val today = LocalDate.now()

        // Group hourly data by date
        val byDate = weekData
            .filter { it.time.length >= 10 }
            .groupBy { it.time.substring(0, 10) }
            .entries
            .sortedBy { it.key }
            .take(3) // Today + 2 days

        return byDate.mapIndexed { index, (dateStr, hours) ->
            val avgWaveHeight = hours.map { it.waveHeightMeters }.average()
            val avgWindSpeed = hours.map { it.windSpeedKmh }.average()
            val avgWindDir = hours.map { it.windDirectionDegrees }.average().toInt()
            val avgTemp = hours.map { it.temperatureCelsius }.average()
            val rating = ConditionRating.forSurf(avgWaveHeight)

            val dayName = when (index) {
                0 -> localizedContext.getString(R.string.widget_today)
                1 -> {
                    try {
                        val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                        date.dayOfWeek.getDisplayName(JavaTextStyle.FULL, locale)
                    } catch (_: Exception) { dateStr }
                }
                else -> {
                    try {
                        val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                        date.dayOfWeek.getDisplayName(JavaTextStyle.FULL, locale)
                    } catch (_: Exception) { dateStr }
                }
            }

            DayForecastState(
                dayName = dayName,
                conditionRating = rating.name,
                conditionRatingDisplay = localizedContext.getString(rating.nameRes),
                waveHeightFormatted = UnitConverter.formatWaveHeightRange(avgWaveHeight, unitSystem),
                windFormatted = WeatherFormatter.formatWindCompact(avgWindSpeed, avgWindDir, unitSystem),
                temperatureFormatted = WeatherFormatter.formatTemperature(avgTemp, unitSystem)
            )
        }
    }

    private suspend fun writeSuccessState(
        cityName: String,
        conditionRating: String,
        conditionRatingDisplay: String,
        temperatureFormatted: String,
        waveHeightFormatted: String,
        windFormatted: String,
        activitiesJson: String,
        dayForecastsJson: String
    ) {
        val widgetClasses = listOf(SmallWidget::class.java, MediumWidget::class.java, LargeWidget::class.java)

        for (widgetClass in widgetClasses) {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(widgetClass)
            for (glanceId in glanceIds) {
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CITY_NAME)] = cityName
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING)] = conditionRating
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING_DISPLAY)] = conditionRatingDisplay
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_TEMPERATURE)] = temperatureFormatted
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_WAVE_HEIGHT)] = waveHeightFormatted
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_WIND)] = windFormatted
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ACTIVITIES_JSON)] = activitiesJson
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_DAY_FORECASTS_JSON)] = dayForecastsJson
                    prefs.remove(stringPreferencesKey(WidgetStateSerializer.KEY_ERROR_MESSAGE))
                    prefs[longPreferencesKey(WidgetStateSerializer.KEY_LAST_UPDATED)] = System.currentTimeMillis()
                }
            }
        }

        // Trigger re-composition
        SmallWidget().updateAll(context)
        MediumWidget().updateAll(context)
        LargeWidget().updateAll(context)
    }

    private suspend fun writeErrorState(errorMessage: String) {
        val widgetClasses = listOf(SmallWidget::class.java, MediumWidget::class.java, LargeWidget::class.java)

        for (widgetClass in widgetClasses) {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(widgetClass)
            for (glanceId in glanceIds) {
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ERROR_MESSAGE)] = errorMessage
                }
            }
        }

        SmallWidget().updateAll(context)
        MediumWidget().updateAll(context)
        LargeWidget().updateAll(context)
    }

    private fun updateContextLocale(context: Context, languageCode: String): Context {
        val locale = if (languageCode == "he") Locale("he") else Locale("en")
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

        return context.createConfigurationContext(configuration)
    }
}
```

**Step 2: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 3: Run tests**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew test --console=plain 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL (GenerateTodaySummaryUseCaseTest should still pass)

**Step 4: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/WidgetUpdateWorker.kt
git commit -m "feat(widget): rewrite WidgetUpdateWorker for Glance state management"
```

---

### Task 8: Update Manifest and Widget Info XMLs

**Files:**
- Modify: `app/src/main/AndroidManifest.xml` — change receiver class names
- Modify: `app/src/main/res/xml/widget_small_info.xml` — update initialLayout
- Modify: `app/src/main/res/xml/widget_medium_info.xml` — update initialLayout
- Modify: `app/src/main/res/xml/widget_large_info.xml` — update initialLayout
- Modify: `app/src/main/res/values/strings.xml` — update widget labels/descriptions

**Step 1: Update manifest receiver declarations**

In `app/src/main/AndroidManifest.xml`, replace the three `<receiver>` blocks (lines 41-75) with:

```xml
        <receiver
            android:name=".widget.SmallWidgetReceiver"
            android:exported="true"
            android:label="@string/widget_small_label">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/widget_small_info" />
        </receiver>

        <receiver
            android:name=".widget.MediumWidgetReceiver"
            android:exported="true"
            android:label="@string/widget_medium_label">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/widget_medium_info" />
        </receiver>

        <receiver
            android:name=".widget.LargeWidgetReceiver"
            android:exported="true"
            android:label="@string/widget_large_label">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/widget_large_info" />
        </receiver>
```

**Step 2: Update widget info XMLs to use Glance default loading layout**

Replace `widget_small_info.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="110dp"
    android:minHeight="110dp"
    android:targetCellWidth="2"
    android:targetCellHeight="2"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:resizeMode="none"
    android:widgetCategory="home_screen"
    android:description="@string/widget_small_description" />
```

Replace `widget_medium_info.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minHeight="110dp"
    android:targetCellWidth="4"
    android:targetCellHeight="2"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:resizeMode="horizontal"
    android:widgetCategory="home_screen"
    android:description="@string/widget_medium_description" />
```

Replace `widget_large_info.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minHeight="250dp"
    android:targetCellWidth="4"
    android:targetCellHeight="3"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:description="@string/widget_large_description" />
```

**Step 3: Update widget string labels and descriptions**

In `app/src/main/res/values/strings.xml`, find and replace the widget label/description strings:

```xml
    <string name="widget_small_label">Beach Forecast — Go/No-Go</string>
    <string name="widget_small_description">Instant signal: is it a good time for the beach?</string>
    <string name="widget_medium_label">Beach Forecast — Activities</string>
    <string name="widget_medium_description">Activity recommendations: surf, swim, kite, SUP.</string>
    <string name="widget_large_label">Beach Forecast — Outlook</string>
    <string name="widget_large_description">3-day forecast with conditions and key metrics.</string>
```

Also update the Hebrew translations in `app/src/main/res/values-he/strings.xml` with corresponding translations.

**Step 4: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add app/src/main/AndroidManifest.xml app/src/main/res/xml/ app/src/main/res/values/strings.xml app/src/main/res/values-he/strings.xml
git commit -m "feat(widget): update manifest and widget info for Glance receivers"
```

---

### Task 9: Delete Old RemoteViews Files

**Files to delete:**
- `app/src/main/java/io/beachforecast/widget/BaseWidgetProvider.kt`
- `app/src/main/java/io/beachforecast/widget/SmallWidgetProvider.kt`
- `app/src/main/java/io/beachforecast/widget/MediumWidgetProvider.kt`
- `app/src/main/java/io/beachforecast/widget/LargeWidgetProvider.kt`
- `app/src/main/java/io/beachforecast/presentation/binders/SmallWidgetBinder.kt`
- `app/src/main/java/io/beachforecast/presentation/binders/MediumWidgetBinder.kt`
- `app/src/main/java/io/beachforecast/presentation/binders/LargeWidgetBinder.kt`
- `app/src/main/java/io/beachforecast/domain/models/WidgetThemeColors.kt`
- `app/src/main/res/layout/widget_small.xml`
- `app/src/main/res/layout/widget_medium.xml`
- `app/src/main/res/layout/widget_large.xml`
- `app/src/main/res/drawable/rounded_card_dark.xml`
- `app/src/main/res/drawable/rounded_card_light.xml`
- `app/src/main/res/drawable/rounded_card_glass_dark.xml`
- `app/src/main/res/drawable/rounded_card_glass_light.xml`
- `app/src/main/res/drawable/rounded_card_background.xml`
- `app/src/main/res/drawable/rounded_card_purple.xml`
- `app/src/main/res/drawable/rounded_card_ocean.xml`

**Step 1: Search for any remaining references to deleted classes**

Search for `WidgetThemeColors`, `SmallWidgetBinder`, `MediumWidgetBinder`, `LargeWidgetBinder`, `BaseWidgetProvider`, `SmallWidgetProvider`, `MediumWidgetProvider`, `LargeWidgetProvider`, `rounded_card_dark`, `rounded_card_light`, `rounded_card_glass`, `widget_small_root`, `widget_medium_root`, `widget_large_root` across the codebase. If any references remain (other than in the files being deleted), update them before deleting.

**Step 2: Delete all old files**

```bash
rm app/src/main/java/io/beachforecast/widget/BaseWidgetProvider.kt
rm app/src/main/java/io/beachforecast/widget/SmallWidgetProvider.kt
rm app/src/main/java/io/beachforecast/widget/MediumWidgetProvider.kt
rm app/src/main/java/io/beachforecast/widget/LargeWidgetProvider.kt
rm app/src/main/java/io/beachforecast/presentation/binders/SmallWidgetBinder.kt
rm app/src/main/java/io/beachforecast/presentation/binders/MediumWidgetBinder.kt
rm app/src/main/java/io/beachforecast/presentation/binders/LargeWidgetBinder.kt
rm app/src/main/java/io/beachforecast/domain/models/WidgetThemeColors.kt
rm app/src/main/res/layout/widget_small.xml
rm app/src/main/res/layout/widget_medium.xml
rm app/src/main/res/layout/widget_large.xml
rm app/src/main/res/drawable/rounded_card_dark.xml
rm app/src/main/res/drawable/rounded_card_light.xml
rm app/src/main/res/drawable/rounded_card_glass_dark.xml
rm app/src/main/res/drawable/rounded_card_glass_light.xml
rm app/src/main/res/drawable/rounded_card_background.xml
rm app/src/main/res/drawable/rounded_card_purple.xml
rm app/src/main/res/drawable/rounded_card_ocean.xml
```

**Step 3: Remove `ConditionRating.getWidgetColor()` extension function**

In `app/src/main/java/io/beachforecast/domain/models/Activity.kt`, delete the `fun ConditionRating.getWidgetColor()` function (lines 71-79) and its import — this was RemoteViews-specific. If any code references it, check and remove those references too.

**Step 4: Verify build and tests**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew test --console=plain 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL. If `ConditionRatingWidgetColorTest` fails, delete it too (it tests the deleted `getWidgetColor()` function).

**Step 5: Commit**

```bash
git add -A
git commit -m "refactor(widget): delete old RemoteViews providers, binders, layouts, and drawables"
```

---

### Task 10: Final Verification and Cleanup

**Step 1: Full build verification**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL

**Step 2: Full test suite**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew test --console=plain`
Expected: BUILD SUCCESSFUL, all tests pass

**Step 3: Check for unused imports and dead code**

Search for any remaining references to deleted classes or files. Grep for:
- `rounded_card` — should find nothing
- `WidgetThemeColors` — should find nothing
- `SmallWidgetBinder` / `MediumWidgetBinder` / `LargeWidgetBinder` — should find nothing
- `BaseWidgetProvider` / `SmallWidgetProvider` / `MediumWidgetProvider` / `LargeWidgetProvider` — should find nothing
- `R.layout.widget_small` / `R.layout.widget_medium` / `R.layout.widget_large` — should find nothing

**Step 4: Clean up unused string resources**

Check if these widget strings are still needed or can be removed:
- `widget_wind`, `widget_swell`, `widget_sea_temp`, `widget_uv_index_label` — likely unused by new widgets (data is pre-formatted)
- `widget_today_forecast`, `widget_now`, `widget_all_day`, `widget_current_status`, `widget_live_vitals` — likely unused
- Keep `widget_today`, `widget_loading`, `widget_error`, content description strings

**Step 5: Release build verification**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleRelease --console=plain 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL (ProGuard shouldn't strip Glance classes if dependencies are correct)

**Step 6: Update ProGuard rules if needed**

If release build fails with missing Glance classes, add to `proguard-rules.pro`:
```
-keep class androidx.glance.** { *; }
```

**Step 7: Commit any cleanup**

```bash
git add -A
git commit -m "chore(widget): cleanup unused resources and verify build"
```
