# Widget Redesign v2 — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace 3 existing Glance widgets with 5 activity-rich widgets that surface sport recommendations, vitals, and weekly forecasts.

**Architecture:** Extend `WidgetUpdateWorker` to compute per-day activity recommendations + vitals for 7 days. Each new widget reads pre-computed state from DataStore via `WidgetStateSerializer`. Shared Glance composable components (header, sport icon row, vitals grid, condition badge) are extracted into `widget/components/`. Old widget files are deleted.

**Tech Stack:** Jetpack Glance 1.1.1, Kotlin, WorkManager, DataStore Preferences, Gson

---

## Task 1: Add sport icon vector drawables (P0)

**Files:**
- Create: `app/src/main/res/drawable/ic_sport_swim.xml`
- Create: `app/src/main/res/drawable/ic_sport_surf.xml`
- Create: `app/src/main/res/drawable/ic_sport_kite.xml`
- Create: `app/src/main/res/drawable/ic_sport_sup.xml`

**Step 1: Create ic_sport_swim.xml (Material Pool icon)**

```xml
<!-- app/src/main/res/drawable/ic_sport_swim.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M22,21c-1.11,0 -1.73,-0.37 -2.18,-0.64 -0.37,-0.22 -0.6,-0.36 -1.15,-0.36 -0.56,0 -0.78,0.13 -1.15,0.36 -0.46,0.27 -1.07,0.64 -2.18,0.64s-1.73,-0.37 -2.18,-0.64c-0.37,-0.22 -0.6,-0.36 -1.15,-0.36 -0.56,0 -0.78,0.13 -1.15,0.36 -0.46,0.27 -1.07,0.64 -2.18,0.64 -1.11,0 -1.73,-0.37 -2.18,-0.64 -0.37,-0.22 -0.6,-0.36 -1.15,-0.36v-2c1.11,0 1.73,0.37 2.18,0.64 0.37,0.22 0.6,0.36 1.15,0.36 0.56,0 0.78,-0.13 1.15,-0.36 0.46,-0.27 1.07,-0.64 2.18,-0.64s1.73,0.37 2.18,0.64c0.37,0.22 0.6,0.36 1.15,0.36 0.56,0 0.78,-0.13 1.15,-0.36 0.45,-0.27 1.07,-0.64 2.18,-0.64 1.11,0 1.73,0.37 2.18,0.64 0.37,0.22 0.6,0.36 1.15,0.36v2c-0.56,0 -0.78,-0.13 -1.15,-0.36 -0.45,-0.27 -1.07,-0.64 -2.18,-0.64zM22,16.3c-1.11,0 -1.73,-0.37 -2.18,-0.64 -0.37,-0.22 -0.6,-0.36 -1.15,-0.36 -0.56,0 -0.78,0.13 -1.15,0.36 -0.45,0.27 -1.07,0.64 -2.18,0.64 -1.11,0 -1.73,-0.37 -2.18,-0.64 -0.37,-0.22 -0.6,-0.36 -1.15,-0.36 -0.56,0 -0.78,0.13 -1.15,0.36 -0.45,0.27 -1.07,0.64 -2.18,0.64 -1.11,0 -1.73,-0.37 -2.18,-0.64 -0.37,-0.22 -0.6,-0.36 -1.15,-0.36v-2c1.11,0 1.73,0.37 2.18,0.64 0.37,0.22 0.6,0.36 1.15,0.36 0.56,0 0.78,-0.13 1.15,-0.36 0.46,-0.27 1.07,-0.64 2.18,-0.64s1.73,0.37 2.18,0.64c0.37,0.22 0.6,0.36 1.15,0.36 0.56,0 0.78,-0.13 1.15,-0.36 0.45,-0.27 1.07,-0.64 2.18,-0.64 1.11,0 1.73,0.37 2.18,0.64 0.37,0.22 0.6,0.36 1.15,0.36v2c-0.56,0 -0.78,-0.13 -1.15,-0.36 -0.45,-0.27 -1.07,-0.64 -2.18,-0.64zM8.67,12c0.56,0 0.78,-0.13 1.15,-0.36 0.46,-0.27 1.07,-0.64 2.18,-0.64 1.11,0 1.73,0.37 2.18,0.64 0.37,0.22 0.6,0.36 1.15,0.36l0.52,-1.72c0.08,-0.26 0.25,-0.48 0.47,-0.63l2.65,-1.81c0.39,-0.26 0.46,-0.79 0.17,-1.18 -0.29,-0.39 -0.82,-0.46 -1.2,-0.17l-2.53,1.73 -1.4,-4.65c-0.17,-0.53 -0.66,-0.89 -1.23,-0.89 -0.58,0 -1.08,0.38 -1.22,0.91L10.48,8.17 7.46,9.69c-0.37,0.19 -0.6,0.56 -0.6,0.97 0,0.62 0.5,1.11 1.12,1.11l0.69,0.23zM12.5,1.5c1.1,0 2,0.9 2,2s-0.9,2 -2,2 -2,-0.9 -2,-2 0.9,-2 2,-2z"/>
</vector>
```

**Step 2: Create ic_sport_surf.xml (Material Surfing icon)**

```xml
<!-- app/src/main/res/drawable/ic_sport_surf.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M21,23c-1.03,0 -2.06,-0.25 -3,-0.75 -1.89,1 -4.11,1 -6,0 -1.89,1 -4.11,1 -6,0 -0.94,0.5 -1.97,0.75 -3,0.75H2v-2h1c1.04,0 2.08,-0.35 3,-1 1.83,1.3 4.17,1.3 6,0 1.83,1.3 4.17,1.3 6,0 0.91,0.65 1.96,1 3,1h1v2h-1zM12,5.5c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM21.98,18.64c-0.19,0.73 -0.59,1.31 -1.12,1.73 -0.91,0.65 -1.96,1 -3,1 -1.03,0 -2.06,-0.25 -3,-0.75 -0.09,-0.05 -0.17,-0.09 -0.26,-0.14L19,14l-5.97,-3.56 -1.07,5.49 -3.52,-2.87c-0.48,-0.39 -0.72,-0.89 -0.72,-1.49s0.24,-1.1 0.72,-1.49L13.4,5.87c0.56,-0.39 1.28,-0.39 1.84,0l4.84,3.61c0.48,0.39 0.72,0.89 0.72,1.49 0,0.14 -0.01,0.28 -0.04,0.42l1.22,7.25z"/>
</vector>
```

**Step 3: Create ic_sport_kite.xml (Material Air icon)**

```xml
<!-- app/src/main/res/drawable/ic_sport_kite.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M14.5,17c0,1.65 -1.35,3 -3,3s-3,-1.35 -3,-3h2c0,0.55 0.45,1 1,1s1,-0.45 1,-1 -0.45,-1 -1,-1L2,16v-2h9.5c1.65,0 3,1.35 3,3zM19,6.5C19,4.57 17.43,3 15.5,3S12,4.57 12,6.5h2c0,-0.83 0.67,-1.5 1.5,-1.5s1.5,0.67 1.5,1.5S16.33,8 15.5,8H2v2h13.5c2.76,0 5,-2.24 5,-5 0,-0.48 -0.07,-0.94 -0.19,-1.38L19,6.5zM18,11H2v2h16c1.65,0 3,1.35 3,3s-1.35,3 -3,3v2c2.76,0 5,-2.24 5,-5s-2.24,-5 -5,-5z"/>
</vector>
```

**Step 4: Create ic_sport_sup.xml (Material Kayaking icon)**

```xml
<!-- app/src/main/res/drawable/ic_sport_sup.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M21,23c-1.03,0 -2.06,-0.25 -3,-0.75 -1.89,1 -4.11,1 -6,0 -1.89,1 -4.11,1 -6,0 -0.94,0.5 -1.97,0.75 -3,0.75H2v-2h1c1.04,0 2.08,-0.35 3,-1 1.83,1.3 4.17,1.3 6,0 1.83,1.3 4.17,1.3 6,0 0.92,0.65 1.96,1 3,1h1v2h-1zM12,5.5c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM2,16l0.71,0.63c0.89,0.78 2.16,0.93 3.2,0.42l5.09,-3.55 2,-1.5 1.42,1.42L21,14l-1.5,-7.5L14,8l-2.1,1.47L6.77,12.58C6.28,12.93 6,13.5 6,14.1c0,0 0,0 0,0L2,16z"/>
</vector>
```

**Step 5: Also create vital icons (wind, swell, sea temp, UV)**

Create `app/src/main/res/drawable/ic_vital_wind.xml`:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M14.5,17c0,1.65 -1.35,3 -3,3s-3,-1.35 -3,-3h2c0,0.55 0.45,1 1,1s1,-0.45 1,-1 -0.45,-1 -1,-1L2,16v-2h9.5c1.65,0 3,1.35 3,3zM19,6.5C19,4.57 17.43,3 15.5,3S12,4.57 12,6.5h2c0,-0.83 0.67,-1.5 1.5,-1.5s1.5,0.67 1.5,1.5S16.33,8 15.5,8H2v2h13.5c2.76,0 5,-2.24 5,-5 0,-0.48 -0.07,-0.94 -0.19,-1.38L19,6.5zM18,11H2v2h16c1.65,0 3,1.35 3,3s-1.35,3 -3,3v2c2.76,0 5,-2.24 5,-5s-2.24,-5 -5,-5z"/>
</vector>
```

Create `app/src/main/res/drawable/ic_vital_waves.xml` (Material Waves icon):
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M2,12c0.55,-0.42 1.31,-0.42 1.86,0 1.46,1.12 3.5,1.12 4.96,0 0.55,-0.42 1.31,-0.42 1.86,0 1.46,1.12 3.5,1.12 4.96,0 0.55,-0.42 1.31,-0.42 1.86,0 0.98,0.75 2.24,1.04 3.5,0.78v2.15c-1.26,0.26 -2.52,-0.03 -3.5,-0.78 -0.55,-0.42 -1.31,-0.42 -1.86,0 -1.46,1.12 -3.5,1.12 -4.96,0 -0.55,-0.42 -1.31,-0.42 -1.86,0 -1.46,1.12 -3.5,1.12 -4.96,0 -0.55,-0.42 -1.31,-0.42 -1.86,0L2,12zM2,7c0.55,-0.42 1.31,-0.42 1.86,0 1.46,1.12 3.5,1.12 4.96,0 0.55,-0.42 1.31,-0.42 1.86,0 1.46,1.12 3.5,1.12 4.96,0 0.55,-0.42 1.31,-0.42 1.86,0 0.98,0.75 2.24,1.04 3.5,0.78v2.15c-1.26,0.26 -2.52,-0.03 -3.5,-0.78 -0.55,-0.42 -1.31,-0.42 -1.86,0 -1.46,1.12 -3.5,1.12 -4.96,0 -0.55,-0.42 -1.31,-0.42 -1.86,0 -1.46,1.12 -3.5,1.12 -4.96,0 -0.55,-0.42 -1.31,-0.42 -1.86,0L2,7zM2,17c0.55,-0.42 1.31,-0.42 1.86,0 1.46,1.12 3.5,1.12 4.96,0 0.55,-0.42 1.31,-0.42 1.86,0 1.46,1.12 3.5,1.12 4.96,0 0.55,-0.42 1.31,-0.42 1.86,0 0.98,0.75 2.24,1.04 3.5,0.78v2.15c-1.26,0.26 -2.52,-0.03 -3.5,-0.78 -0.55,-0.42 -1.31,-0.42 -1.86,0 -1.46,1.12 -3.5,1.12 -4.96,0 -0.55,-0.42 -1.31,-0.42 -1.86,0 -1.46,1.12 -3.5,1.12 -4.96,0 -0.55,-0.42 -1.31,-0.42 -1.86,0L2,17z"/>
</vector>
```

Create `app/src/main/res/drawable/ic_vital_sea_temp.xml` (Material Thermostat icon):
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M15,13V5c0,-1.66 -1.34,-3 -3,-3S9,3.34 9,5v8c-1.21,0.91 -2,2.37 -2,4 0,2.76 2.24,5 5,5s5,-2.24 5,-5c0,-1.63 -0.79,-3.09 -2,-4zM11,5c0,-0.55 0.45,-1 1,-1s1,0.45 1,1h-1v1h1v2h-1v1h1v2h-2V5z"/>
</vector>
```

Create `app/src/main/res/drawable/ic_vital_uv.xml` (Material WbSunny icon):
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M6.76,4.84l-1.8,-1.79 -1.41,1.41 1.79,1.79 1.42,-1.41zM4,10.5H1v2h3v-2zM13,0.55h-2V3.5h2V0.55zM20.45,4.46l-1.41,-1.41 -1.79,1.79 1.41,1.41 1.79,-1.79zM17.24,18.16l1.79,1.8 1.41,-1.41 -1.8,-1.79 -1.4,1.4zM20,10.5v2h3v-2h-3zM12,5.5c-3.31,0 -6,2.69 -6,6s2.69,6 6,6 6,-2.69 6,-6 -2.69,-6 -6,-6zM11,22.45h2V19.5h-2v2.95zM3.55,18.54l1.41,1.41 1.79,-1.8 -1.41,-1.41 -1.79,1.8z"/>
</vector>
```

**Step 6: Commit**

```bash
git add app/src/main/res/drawable/ic_sport_*.xml app/src/main/res/drawable/ic_vital_*.xml
git commit -m "feat(widget): add sport and vital vector drawable icons"
```

---

## Task 2: Extend WidgetState with new data models (P0)

**Files:**
- Modify: `app/src/main/java/io/beachforecast/widget/WidgetState.kt`

**Step 1: Add `activityKey` field to ActivityState**

In `WidgetState.kt`, replace the `ActivityState` data class (line 23-27):

```kotlin
// CURRENT:
data class ActivityState(
    val name: String,           // Localized activity name
    val isRecommended: Boolean,
    val reason: String          // Localized reason text
)

// REPLACE WITH:
data class ActivityState(
    val name: String,           // Localized activity name
    val activityKey: String,    // Activity enum name (SWIM, SURF, KITE, SUP)
    val isRecommended: Boolean,
    val isPrimary: Boolean = false,
    val reason: String          // Localized reason text
)
```

**Step 2: Add `activitiesJson` field to DayForecastState**

In `WidgetState.kt`, replace the `DayForecastState` data class (line 29-36):

```kotlin
// CURRENT:
data class DayForecastState(
    val dayName: String,
    val conditionRating: String,
    val conditionRatingDisplay: String,
    val waveHeightFormatted: String,
    val windFormatted: String,
    val temperatureFormatted: String
)

// REPLACE WITH:
data class DayForecastState(
    val dayName: String,
    val conditionRating: String,
    val conditionRatingDisplay: String,
    val waveHeightFormatted: String,
    val windFormatted: String,
    val temperatureFormatted: String,
    val activities: List<ActivityState> = emptyList()
)
```

**Step 3: Add VitalState data class and new serializer keys**

After `DayForecastState`, add:

```kotlin
data class VitalState(
    val label: String,
    val value: String,
    val iconType: String,        // "wind", "swell", "sea_temp", "uv"
    val accentColor: String? = null  // Optional hex color for UV severity
)
```

In `WidgetStateSerializer`, add new keys after `KEY_LAST_UPDATED` (line 54):

```kotlin
const val KEY_VITALS_JSON = "widget_vitals_json"
const val KEY_WEEK_FORECASTS_JSON = "widget_week_forecasts_json"
const val KEY_BEST_DAY_INDEX = "widget_best_day_index"
const val KEY_BEST_DAY_SUMMARY = "widget_best_day_summary"
```

Add serialization methods at the end of the object:

```kotlin
fun serializeVitals(vitals: List<VitalState>): String = gson.toJson(vitals)

fun deserializeVitals(json: String): List<VitalState> {
    if (json.isBlank()) return emptyList()
    val type = object : TypeToken<List<VitalState>>() {}.type
    return gson.fromJson(json, type)
}
```

**Step 4: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/WidgetState.kt
git commit -m "feat(widget): extend state models with activity keys, vitals, and week data"
```

---

## Task 3: Add new string resources (P0)

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-he/strings.xml`

**Step 1: Replace old widget strings and add new ones in `values/strings.xml`**

Find the block starting at `widget_small_label` (line 127) through `widget_today` (line 135). Replace with:

```xml
    <!-- Widget labels and descriptions -->
    <string name="widget_quick_glance_label">Beach Forecast — Glance</string>
    <string name="widget_quick_glance_description">Quick look: condition rating and sport recommendations.</string>
    <string name="widget_today_forecast_label">Beach Forecast — Today</string>
    <string name="widget_today_forecast_description">Today\'s sport recommendations with reasons.</string>
    <string name="widget_today_vitals_label">Beach Forecast — Vitals</string>
    <string name="widget_today_vitals_description">Sport recommendations plus live wind, swell, sea temp, and UV.</string>
    <string name="widget_two_day_label">Beach Forecast — 2-Day</string>
    <string name="widget_two_day_description">Compare today and tomorrow: conditions, sports, and key metrics.</string>
    <string name="widget_week_planner_label">Beach Forecast — Week</string>
    <string name="widget_week_planner_description">Weekly planner: best days for each sport at a glance.</string>
    <string name="widget_loading">Loading…</string>
    <string name="widget_error">Error</string>
    <string name="widget_today">TODAY</string>
    <string name="widget_tomorrow">TOMORROW</string>
    <string name="widget_week_forecast">Week Forecast</string>
    <string name="widget_best_day">Best: %1$s</string>
    <string name="widget_vital_wind">Wind</string>
    <string name="widget_vital_swell">Swell</string>
    <string name="widget_vital_sea_temp">Sea Temp</string>
    <string name="widget_vital_uv">UV</string>
```

**Step 2: Add Hebrew translations in `values-he/strings.xml`**

Add (or replace old widget strings with) the following Hebrew strings. Place them after existing widget strings:

```xml
    <!-- Widget labels and descriptions -->
    <string name="widget_quick_glance_label">תחזית חוף — מבט</string>
    <string name="widget_quick_glance_description">מבט מהיר: דירוג תנאים והמלצות ספורט.</string>
    <string name="widget_today_forecast_label">תחזית חוף — היום</string>
    <string name="widget_today_forecast_description">המלצות ספורט להיום עם סיבות.</string>
    <string name="widget_today_vitals_label">תחזית חוף — נתונים</string>
    <string name="widget_today_vitals_description">המלצות ספורט בתוספת רוח, גלישה, טמפרטורת ים ו-UV.</string>
    <string name="widget_two_day_label">תחזית חוף — יומיים</string>
    <string name="widget_two_day_description">השוואת היום ומחר: תנאים, ספורט ומדדים.</string>
    <string name="widget_week_planner_label">תחזית חוף — שבוע</string>
    <string name="widget_week_planner_description">תכנון שבועי: הימים הטובים לכל ספורט במבט אחד.</string>
    <string name="widget_today">היום</string>
    <string name="widget_tomorrow">מחר</string>
    <string name="widget_week_forecast">תחזית שבועית</string>
    <string name="widget_best_day">הכי טוב: %1$s</string>
    <string name="widget_vital_wind">רוח</string>
    <string name="widget_vital_swell">גלים</string>
    <string name="widget_vital_sea_temp">טמפ\' ים</string>
    <string name="widget_vital_uv">UV</string>
```

**Step 3: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/res/values/strings.xml app/src/main/res/values-he/strings.xml
git commit -m "feat(widget): add string resources for 5 new widgets (en + he)"
```

---

## Task 4: Create shared Glance widget components (P0)

**Files:**
- Create: `app/src/main/java/io/beachforecast/widget/components/WidgetComponents.kt`

**Step 1: Create the shared components file**

This file contains reusable Glance composables used across multiple widgets: `WidgetHeader`, `SportIconRow`, `SportCard`, `ConditionBadge`, `VitalsGrid`, `VitalCard`, `WidgetLoadingContent`, `WidgetErrorContent`.

```kotlin
package io.beachforecast.widget.components

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
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
import io.beachforecast.widget.ActivityState
import io.beachforecast.widget.VitalState
import io.beachforecast.widget.theme.ConditionColors

private val RecommendedColor = Color(0xFF4CAF50)
private val NotRecommendedColor = Color(0xFF78909C)

/**
 * Standard widget header: app icon + city name + right-side content.
 */
@Composable
fun WidgetHeader(
    cityName: String,
    trailingContent: @Composable () -> Unit = {}
) {
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
        trailingContent()
    }
}

/**
 * Condition rating badge with colored text.
 */
@Composable
fun ConditionBadge(
    conditionRating: String,
    conditionDisplay: String,
    fontSize: Float = 11f
) {
    val color = ConditionColors.forRating(conditionRating)
    Text(
        text = conditionDisplay,
        style = TextStyle(
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            color = ColorProvider(color)
        ),
        maxLines = 1
    )
}

/**
 * Row of sport icons showing recommendation status.
 * Used in Quick Glance (icons only) and Today's Forecast (cards with reasons).
 */
@Composable
fun SportIconRow(
    activities: List<ActivityState>,
    showReasons: Boolean = false,
    modifier: GlanceModifier = GlanceModifier.fillMaxWidth()
) {
    Row(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        activities.forEachIndexed { index, activity ->
            if (index > 0) Spacer(modifier = GlanceModifier.size(4.dp))
            if (showReasons) {
                SportCard(
                    activity = activity,
                    modifier = GlanceModifier.defaultWeight()
                )
            } else {
                SportIconCompact(
                    activity = activity,
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }
    }
}

/**
 * Compact sport icon with name — for Quick Glance and Two-Day widgets.
 */
@Composable
fun SportIconCompact(
    activity: ActivityState,
    modifier: GlanceModifier = GlanceModifier
) {
    val tintColor = if (activity.isRecommended) RecommendedColor else NotRecommendedColor
    val alpha = if (activity.isRecommended) 1f else 0.4f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = GlanceModifier
                .size(32.dp)
                .background(ColorProvider(tintColor.copy(alpha = 0.15f)))
                .cornerRadius(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(sportIconRes(activity.activityKey)),
                contentDescription = activity.name,
                modifier = GlanceModifier.size(18.dp),
                colorFilter = ColorFilter.tint(ColorProvider(tintColor.copy(alpha = alpha)))
            )
        }
        Spacer(modifier = GlanceModifier.size(2.dp))
        Text(
            text = activity.name,
            style = TextStyle(
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onSurfaceVariant
            ),
            maxLines = 1
        )
    }
}

/**
 * Sport card with icon, name, and reason — for Today's Forecast and Today+Vitals.
 */
@Composable
fun SportCard(
    activity: ActivityState,
    modifier: GlanceModifier = GlanceModifier
) {
    val tintColor = if (activity.isRecommended) RecommendedColor else NotRecommendedColor
    val dotColor = if (activity.isRecommended) RecommendedColor else NotRecommendedColor

    Column(
        modifier = modifier
            .background(GlanceTheme.colors.surfaceVariant)
            .cornerRadius(12.dp)
            .padding(8.dp)
    ) {
        // Icon + name row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(sportIconRes(activity.activityKey)),
                contentDescription = activity.name,
                modifier = GlanceModifier.size(16.dp),
                colorFilter = ColorFilter.tint(ColorProvider(tintColor))
            )
            Spacer(modifier = GlanceModifier.size(4.dp))
            Text(
                text = activity.name,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.size(2.dp))

        // Status dot + reason
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = GlanceModifier
                    .size(6.dp)
                    .background(ColorProvider(dotColor))
                    .cornerRadius(3.dp)
            ) {}
            Spacer(modifier = GlanceModifier.size(3.dp))
            Text(
                text = activity.reason,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * 2x2 grid of vital metrics.
 */
@Composable
fun VitalsGrid(vitals: List<VitalState>) {
    if (vitals.isEmpty()) return

    val firstRow = vitals.take(2)
    val secondRow = vitals.drop(2).take(2)

    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            firstRow.forEachIndexed { index, vital ->
                if (index > 0) Spacer(modifier = GlanceModifier.size(4.dp))
                VitalCard(vital, GlanceModifier.defaultWeight())
            }
        }
        if (secondRow.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.size(4.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                secondRow.forEachIndexed { index, vital ->
                    if (index > 0) Spacer(modifier = GlanceModifier.size(4.dp))
                    VitalCard(vital, GlanceModifier.defaultWeight())
                }
            }
        }
    }
}

/**
 * Single vital metric card.
 */
@Composable
fun VitalCard(vital: VitalState, modifier: GlanceModifier = GlanceModifier) {
    val iconRes = vitalIconRes(vital.iconType)
    val accentColor = vital.accentColor?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { null }
    }
    val iconTint = accentColor ?: GlanceTheme.colors.onSurfaceVariant

    Row(
        modifier = modifier
            .background(GlanceTheme.colors.surfaceVariant)
            .cornerRadius(8.dp)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = vital.label,
            modifier = GlanceModifier.size(14.dp),
            colorFilter = if (accentColor != null) {
                ColorFilter.tint(ColorProvider(accentColor))
            } else {
                ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
            }
        )
        Spacer(modifier = GlanceModifier.size(4.dp))
        Column {
            Text(
                text = vital.value,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )
            Text(
                text = vital.label,
                style = TextStyle(
                    fontSize = 9.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * Condition color bar spanning full width.
 */
@Composable
fun ConditionBar(conditionRating: String) {
    val color = ConditionColors.forRating(conditionRating)
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(4.dp)
            .background(ColorProvider(color))
            .cornerRadius(2.dp)
    ) {}
}

/**
 * Standard loading content used by all widgets.
 */
@Composable
fun WidgetLoadingContent() {
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
            text = "Loading…",
            style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant)
        )
    }
}

/**
 * Standard error content used by all widgets.
 */
@Composable
fun WidgetErrorContent(message: String) {
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

/**
 * Maps Activity enum name to vector drawable resource ID.
 */
fun sportIconRes(activityKey: String): Int = when (activityKey) {
    "SWIM" -> R.drawable.ic_sport_swim
    "SURF" -> R.drawable.ic_sport_surf
    "KITE" -> R.drawable.ic_sport_kite
    "SUP" -> R.drawable.ic_sport_sup
    else -> R.drawable.ic_sport_swim
}

/**
 * Maps vital type to vector drawable resource ID.
 */
fun vitalIconRes(iconType: String): Int = when (iconType) {
    "wind" -> R.drawable.ic_vital_wind
    "swell" -> R.drawable.ic_vital_waves
    "sea_temp" -> R.drawable.ic_vital_sea_temp
    "uv" -> R.drawable.ic_vital_uv
    else -> R.drawable.ic_vital_wind
}
```

**Step 2: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/components/WidgetComponents.kt
git commit -m "feat(widget): add shared Glance composable components"
```

---

## Task 5: Create Widget 1 — Quick Glance (2x2) (P0)

**Files:**
- Create: `app/src/main/java/io/beachforecast/widget/QuickGlanceWidget.kt`
- Create: `app/src/main/res/xml/widget_quick_glance_info.xml`

**Step 1: Create the widget info XML**

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
    android:description="@string/widget_quick_glance_description" />
```

**Step 2: Create QuickGlanceWidget.kt**

```kotlin
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
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.beachforecast.MainActivity
import io.beachforecast.widget.components.ConditionBar
import io.beachforecast.widget.components.ConditionBadge
import io.beachforecast.widget.components.SportIconRow
import io.beachforecast.widget.components.WidgetErrorContent
import io.beachforecast.widget.components.WidgetHeader
import io.beachforecast.widget.components.WidgetLoadingContent
import io.beachforecast.widget.theme.BeachForecastWidgetTheme

class QuickGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BeachForecastWidgetTheme {
                QuickGlanceContent()
            }
        }
    }
}

class QuickGlanceWidgetReceiver : BeachForecastWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickGlanceWidget()
}

@Composable
private fun QuickGlanceContent() {
    val context = LocalContext.current
    val prefs = currentState<Preferences>()
    val cityName = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CITY_NAME)] ?: ""
    val conditionRating = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING)] ?: ""
    val conditionDisplay = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING_DISPLAY)] ?: ""
    val temperature = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_TEMPERATURE)] ?: ""
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
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> WidgetLoadingContent()
            hasError -> WidgetErrorContent(errorMessage!!)
            else -> QuickGlanceBody(cityName, temperature, conditionRating, conditionDisplay, activities)
        }
    }
}

@Composable
private fun QuickGlanceBody(
    cityName: String,
    temperature: String,
    conditionRating: String,
    conditionDisplay: String,
    activities: List<ActivityState>
) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header: city name + temp
        WidgetHeader(cityName) {
            Text(
                text = temperature,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Sport icons row (compact, no reasons)
        SportIconRow(activities = activities, showReasons = false)

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Condition bar at bottom
        ConditionBadge(conditionRating, conditionDisplay, fontSize = 14f)
        Spacer(modifier = GlanceModifier.size(4.dp))
        ConditionBar(conditionRating)
    }
}
```

**Step 3: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/QuickGlanceWidget.kt app/src/main/res/xml/widget_quick_glance_info.xml
git commit -m "feat(widget): add Quick Glance widget (2x2)"
```

---

## Task 6: Create Widget 2 — Today's Forecast (4x2) (P0)

**Files:**
- Create: `app/src/main/java/io/beachforecast/widget/TodayForecastWidget.kt`
- Create: `app/src/main/res/xml/widget_today_forecast_info.xml`

**Step 1: Create the widget info XML**

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
    android:description="@string/widget_today_forecast_description" />
```

**Step 2: Create TodayForecastWidget.kt**

```kotlin
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
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.beachforecast.MainActivity
import io.beachforecast.widget.components.ConditionBadge
import io.beachforecast.widget.components.SportIconRow
import io.beachforecast.widget.components.WidgetErrorContent
import io.beachforecast.widget.components.WidgetHeader
import io.beachforecast.widget.components.WidgetLoadingContent
import io.beachforecast.widget.theme.BeachForecastWidgetTheme

class TodayForecastWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BeachForecastWidgetTheme {
                TodayForecastContent()
            }
        }
    }
}

class TodayForecastWidgetReceiver : BeachForecastWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayForecastWidget()
}

@Composable
private fun TodayForecastContent() {
    val context = LocalContext.current
    val prefs = currentState<Preferences>()
    val cityName = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CITY_NAME)] ?: ""
    val conditionRating = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING)] ?: ""
    val conditionDisplay = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING_DISPLAY)] ?: ""
    val temperature = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_TEMPERATURE)] ?: ""
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
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        when {
            isLoading -> WidgetLoadingContent()
            hasError -> WidgetErrorContent(errorMessage!!)
            else -> TodayForecastBody(cityName, temperature, conditionRating, conditionDisplay, activities)
        }
    }
}

@Composable
private fun TodayForecastBody(
    cityName: String,
    temperature: String,
    conditionRating: String,
    conditionDisplay: String,
    activities: List<ActivityState>
) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp)
    ) {
        // Header: city name + condition badge + temperature
        WidgetHeader(cityName) {
            ConditionBadge(conditionRating, conditionDisplay)
            Spacer(modifier = GlanceModifier.size(6.dp))
            Text(
                text = temperature,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }

        Spacer(modifier = GlanceModifier.size(8.dp))

        // Sport cards with reasons
        when {
            activities.size <= 3 -> SportIconRow(activities = activities, showReasons = true)
            else -> {
                SportIconRow(activities = activities.take(2), showReasons = true)
                Spacer(modifier = GlanceModifier.size(4.dp))
                SportIconRow(activities = activities.drop(2).take(2), showReasons = true)
            }
        }
    }
}
```

**Step 3: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/TodayForecastWidget.kt app/src/main/res/xml/widget_today_forecast_info.xml
git commit -m "feat(widget): add Today's Forecast widget (4x2)"
```

---

## Task 7: Create Widget 3 — Today + Vitals (4x3) (P0)

**Files:**
- Create: `app/src/main/java/io/beachforecast/widget/TodayVitalsWidget.kt`
- Create: `app/src/main/res/xml/widget_today_vitals_info.xml`

**Step 1: Create widget info XML**

```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minHeight="180dp"
    android:targetCellWidth="4"
    android:targetCellHeight="3"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:description="@string/widget_today_vitals_description" />
```

**Step 2: Create TodayVitalsWidget.kt**

```kotlin
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
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.beachforecast.MainActivity
import io.beachforecast.widget.components.ConditionBadge
import io.beachforecast.widget.components.SportIconRow
import io.beachforecast.widget.components.VitalsGrid
import io.beachforecast.widget.components.WidgetErrorContent
import io.beachforecast.widget.components.WidgetHeader
import io.beachforecast.widget.components.WidgetLoadingContent
import io.beachforecast.widget.theme.BeachForecastWidgetTheme

class TodayVitalsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BeachForecastWidgetTheme {
                TodayVitalsContent()
            }
        }
    }
}

class TodayVitalsWidgetReceiver : BeachForecastWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayVitalsWidget()
}

@Composable
private fun TodayVitalsContent() {
    val context = LocalContext.current
    val prefs = currentState<Preferences>()
    val cityName = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CITY_NAME)] ?: ""
    val conditionRating = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING)] ?: ""
    val conditionDisplay = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CONDITION_RATING_DISPLAY)] ?: ""
    val temperature = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_TEMPERATURE)] ?: ""
    val activitiesJson = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ACTIVITIES_JSON)] ?: ""
    val vitalsJson = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_VITALS_JSON)] ?: ""
    val errorMessage = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ERROR_MESSAGE)]
    val lastUpdated = prefs[longPreferencesKey(WidgetStateSerializer.KEY_LAST_UPDATED)] ?: 0L

    val isLoading = lastUpdated == 0L && errorMessage == null
    val hasError = errorMessage != null
    val activities = WidgetStateSerializer.deserializeActivities(activitiesJson)
    val vitals = WidgetStateSerializer.deserializeVitals(vitalsJson)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        when {
            isLoading -> WidgetLoadingContent()
            hasError -> WidgetErrorContent(errorMessage!!)
            else -> TodayVitalsBody(cityName, temperature, conditionRating, conditionDisplay, activities, vitals)
        }
    }
}

@Composable
private fun TodayVitalsBody(
    cityName: String,
    temperature: String,
    conditionRating: String,
    conditionDisplay: String,
    activities: List<ActivityState>,
    vitals: List<VitalState>
) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp)
    ) {
        // Header
        WidgetHeader(cityName) {
            ConditionBadge(conditionRating, conditionDisplay)
            Spacer(modifier = GlanceModifier.size(6.dp))
            Text(
                text = temperature,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }

        Spacer(modifier = GlanceModifier.size(6.dp))

        // Sport cards (compact row to save space)
        SportIconRow(activities = activities, showReasons = true)

        Spacer(modifier = GlanceModifier.size(6.dp))

        // Vitals 2x2 grid
        VitalsGrid(vitals)
    }
}
```

**Step 3: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/TodayVitalsWidget.kt app/src/main/res/xml/widget_today_vitals_info.xml
git commit -m "feat(widget): add Today + Vitals widget (4x3)"
```

---

## Task 8: Create Widget 4 — Two-Day Planner (4x3) (P0)

**Files:**
- Create: `app/src/main/java/io/beachforecast/widget/TwoDayPlannerWidget.kt`
- Create: `app/src/main/res/xml/widget_two_day_info.xml`

**Step 1: Create widget info XML**

```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minHeight="180dp"
    android:targetCellWidth="4"
    android:targetCellHeight="3"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:description="@string/widget_two_day_description" />
```

**Step 2: Create TwoDayPlannerWidget.kt**

```kotlin
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
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.beachforecast.MainActivity
import io.beachforecast.widget.components.ConditionBadge
import io.beachforecast.widget.components.SportIconRow
import io.beachforecast.widget.components.WidgetErrorContent
import io.beachforecast.widget.components.WidgetHeader
import io.beachforecast.widget.components.WidgetLoadingContent
import io.beachforecast.widget.theme.BeachForecastWidgetTheme

class TwoDayPlannerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BeachForecastWidgetTheme {
                TwoDayPlannerContent()
            }
        }
    }
}

class TwoDayPlannerWidgetReceiver : BeachForecastWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TwoDayPlannerWidget()
}

@Composable
private fun TwoDayPlannerContent() {
    val context = LocalContext.current
    val prefs = currentState<Preferences>()
    val cityName = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CITY_NAME)] ?: ""
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
            isLoading -> WidgetLoadingContent()
            hasError -> WidgetErrorContent(errorMessage!!)
            dayForecasts.size >= 2 -> TwoDayBody(cityName, dayForecasts[0], dayForecasts[1])
            else -> WidgetErrorContent("No forecast data")
        }
    }
}

@Composable
private fun TwoDayBody(
    cityName: String,
    today: DayForecastState,
    tomorrow: DayForecastState
) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp)
    ) {
        // Header
        WidgetHeader(cityName)

        Spacer(modifier = GlanceModifier.size(6.dp))

        // Two columns side-by-side
        Row(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight()
        ) {
            DayColumn(
                day = today,
                isHighlighted = true,
                modifier = GlanceModifier.defaultWeight()
            )
            Spacer(modifier = GlanceModifier.size(6.dp))
            DayColumn(
                day = tomorrow,
                isHighlighted = false,
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }
}

@Composable
private fun DayColumn(
    day: DayForecastState,
    isHighlighted: Boolean,
    modifier: GlanceModifier
) {
    val background = if (isHighlighted) {
        GlanceTheme.colors.surfaceVariant
    } else {
        GlanceTheme.colors.widgetBackground
    }

    Column(
        modifier = modifier
            .background(background)
            .cornerRadius(12.dp)
            .padding(8.dp)
    ) {
        // Day name
        Text(
            text = day.dayName,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )

        Spacer(modifier = GlanceModifier.size(4.dp))

        // Condition badge + temperature
        Row(verticalAlignment = Alignment.CenterVertically) {
            ConditionBadge(day.conditionRating, day.conditionRatingDisplay, fontSize = 11f)
            Spacer(modifier = GlanceModifier.size(4.dp))
            Text(
                text = day.temperatureFormatted,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }

        Spacer(modifier = GlanceModifier.size(6.dp))

        // Sport icons (compact, no reasons)
        if (day.activities.isNotEmpty()) {
            SportIconRow(
                activities = day.activities,
                showReasons = false
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Key metrics
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = day.waveHeightFormatted,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = day.windFormatted,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}
```

**Step 3: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/TwoDayPlannerWidget.kt app/src/main/res/xml/widget_two_day_info.xml
git commit -m "feat(widget): add Two-Day Planner widget (4x3)"
```

---

## Task 9: Create Widget 5 — Week Planner (4x4) (P0)

**Files:**
- Create: `app/src/main/java/io/beachforecast/widget/WeekPlannerWidget.kt`
- Create: `app/src/main/res/xml/widget_week_planner_info.xml`

**Step 1: Create widget info XML**

```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minHeight="250dp"
    android:targetCellWidth="4"
    android:targetCellHeight="4"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:description="@string/widget_week_planner_description" />
```

**Step 2: Create WeekPlannerWidget.kt**

```kotlin
package io.beachforecast.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.beachforecast.MainActivity
import io.beachforecast.widget.components.WidgetErrorContent
import io.beachforecast.widget.components.WidgetHeader
import io.beachforecast.widget.components.WidgetLoadingContent
import io.beachforecast.widget.components.sportIconRes
import io.beachforecast.widget.theme.BeachForecastWidgetTheme
import io.beachforecast.widget.theme.ConditionColors

private val RecommendedColor = Color(0xFF4CAF50)
private val NotRecommendedColor = Color(0xFF78909C)

class WeekPlannerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            BeachForecastWidgetTheme {
                WeekPlannerContent()
            }
        }
    }
}

class WeekPlannerWidgetReceiver : BeachForecastWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeekPlannerWidget()
}

@Composable
private fun WeekPlannerContent() {
    val context = LocalContext.current
    val prefs = currentState<Preferences>()
    val cityName = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_CITY_NAME)] ?: ""
    val weekForecastsJson = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_WEEK_FORECASTS_JSON)] ?: ""
    val bestDayIndex = prefs[intPreferencesKey(WidgetStateSerializer.KEY_BEST_DAY_INDEX)] ?: -1
    val bestDaySummary = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_BEST_DAY_SUMMARY)] ?: ""
    val errorMessage = prefs[stringPreferencesKey(WidgetStateSerializer.KEY_ERROR_MESSAGE)]
    val lastUpdated = prefs[longPreferencesKey(WidgetStateSerializer.KEY_LAST_UPDATED)] ?: 0L

    val isLoading = lastUpdated == 0L && errorMessage == null
    val hasError = errorMessage != null
    val weekForecasts = WidgetStateSerializer.deserializeDayForecasts(weekForecastsJson)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        when {
            isLoading -> WidgetLoadingContent()
            hasError -> WidgetErrorContent(errorMessage!!)
            weekForecasts.isEmpty() -> WidgetErrorContent("No forecast data")
            else -> WeekPlannerBody(cityName, weekForecasts, bestDayIndex, bestDaySummary)
        }
    }
}

@Composable
private fun WeekPlannerBody(
    cityName: String,
    days: List<DayForecastState>,
    bestDayIndex: Int,
    bestDaySummary: String
) {
    // Collect unique sport keys across all days
    val sportKeys = days
        .flatMap { it.activities }
        .map { it.activityKey }
        .distinct()

    Column(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp)
    ) {
        // Header: city name + "Week Forecast"
        WidgetHeader(cityName) {
            Text(
                text = "Week Forecast",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = GlanceModifier.size(6.dp))

        // Day name header row
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            // Left spacer for sport icon column
            Spacer(modifier = GlanceModifier.size(28.dp))
            days.forEachIndexed { index, day ->
                if (index > 0) Spacer(modifier = GlanceModifier.size(2.dp))
                val isBest = index == bestDayIndex
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .let {
                            if (isBest) it.background(GlanceTheme.colors.surfaceVariant).cornerRadius(4.dp)
                            else it
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.dayName.take(3),
                        style = TextStyle(
                            fontSize = 9.sp,
                            fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal,
                            color = GlanceTheme.colors.onSurfaceVariant
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.size(4.dp))

        // Sport rows: one row per sport
        sportKeys.forEach { sportKey ->
            SportWeekRow(sportKey, days, bestDayIndex)
            Spacer(modifier = GlanceModifier.size(2.dp))
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Best day footer
        if (bestDaySummary.isNotEmpty()) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(8.dp)
                    .padding(8.dp)
            ) {
                Text(
                    text = bestDaySummary,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onSurface
                    ),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun SportWeekRow(
    sportKey: String,
    days: List<DayForecastState>,
    bestDayIndex: Int
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sport icon on the left
        Image(
            provider = ImageProvider(sportIconRes(sportKey)),
            contentDescription = sportKey,
            modifier = GlanceModifier.size(20.dp),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
        )
        Spacer(modifier = GlanceModifier.size(8.dp))

        // Dots for each day
        days.forEachIndexed { index, day ->
            if (index > 0) Spacer(modifier = GlanceModifier.size(2.dp))
            val isRecommended = day.activities.find { it.activityKey == sportKey }?.isRecommended ?: false
            val dotColor = if (isRecommended) RecommendedColor else NotRecommendedColor
            val isBest = index == bestDayIndex

            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .let {
                        if (isBest) it.background(GlanceTheme.colors.surfaceVariant).cornerRadius(4.dp)
                        else it
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(10.dp)
                        .background(ColorProvider(dotColor))
                        .cornerRadius(5.dp)
                ) {}
            }
        }
    }
}
```

**Step 3: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/WeekPlannerWidget.kt app/src/main/res/xml/widget_week_planner_info.xml
git commit -m "feat(widget): add Week Planner widget (4x4)"
```

---

## Task 10: Extend WidgetUpdateWorker for all 5 widgets (P0)

**Files:**
- Modify: `app/src/main/java/io/beachforecast/widget/WidgetUpdateWorker.kt`

This is the most critical task. The worker needs to compute:
1. Activity recommendations for today (existing) + per-day for 7 days (new)
2. Vitals from current conditions (new)
3. Best day determination (new)
4. Update all 5 widget types (replace existing 3)

**Step 1: Update imports**

In `WidgetUpdateWorker.kt`, add these imports after the existing ones (line 26):

```kotlin
import androidx.datastore.preferences.core.intPreferencesKey
import io.beachforecast.domain.models.Activity
```

**Step 2: Update `computeDayForecasts` to include per-day activity recommendations**

Replace the `computeDayForecasts` method (lines 117-161) with:

```kotlin
    private fun computeDayForecasts(
        weekData: List<HourlyWaveForecast>,
        unitSystem: UnitSystem,
        localizedContext: Context,
        languageCode: String,
        selectedSports: Set<Activity>,
        maxDays: Int = 7
    ): List<DayForecastState> {
        if (weekData.isEmpty()) return emptyList()

        val locale = if (languageCode == "he") Locale("he") else Locale("en")

        val byDate = weekData
            .filter { it.time.length >= 10 }
            .groupBy { it.time.substring(0, 10) }
            .entries
            .sortedBy { it.key }
            .take(maxDays)

        return byDate.mapIndexed { index, (dateStr, hours) ->
            val avgWaveHeight = hours.map { it.waveHeightMeters }.average()
            val avgWindSpeed = hours.map { it.windSpeedKmh }.average()
            val avgWindDir = hours.map { it.windDirectionDegrees }.average().toInt()
            val avgTemp = hours.map { it.temperatureCelsius }.average()
            val avgSwell = hours.map { it.swellHeightMeters }.average()
            val rating = ConditionRating.forSurf(avgWaveHeight)

            val dayName = when (index) {
                0 -> localizedContext.getString(R.string.widget_today)
                1 -> localizedContext.getString(R.string.widget_tomorrow)
                else -> {
                    try {
                        val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                        date.dayOfWeek.getDisplayName(JavaTextStyle.FULL, locale)
                    } catch (_: Exception) { dateStr }
                }
            }

            // Compute per-day activity recommendations using averaged conditions
            val dayConditions = io.beachforecast.domain.models.CurrentConditions(
                waveCategory = io.beachforecast.domain.models.WaveCategory.fromHeight(avgWaveHeight),
                waveHeightMeters = avgWaveHeight,
                temperatureCelsius = avgTemp,
                cloudCover = io.beachforecast.domain.models.CloudCoverLevel.PARTLY_CLOUDY,
                windSpeedKmh = avgWindSpeed,
                windDirectionDegrees = avgWindDir,
                swellHeightMeters = avgSwell
            )
            val dayRecs = ActivityRecommendationCalculator.calculateForSports(dayConditions, selectedSports)
            val dayActivities = dayRecs.recommendations.map { rec ->
                ActivityState(
                    name = localizedContext.getString(rec.activity.nameRes),
                    activityKey = rec.activity.name,
                    isRecommended = rec.isRecommended,
                    isPrimary = rec.isPrimary,
                    reason = rec.reason ?: ""
                )
            }

            DayForecastState(
                dayName = dayName,
                conditionRating = rating.name,
                conditionRatingDisplay = localizedContext.getString(rating.nameRes),
                waveHeightFormatted = UnitConverter.formatWaveHeightRange(avgWaveHeight, unitSystem),
                windFormatted = WeatherFormatter.formatWindCompact(avgWindSpeed, avgWindDir, unitSystem),
                temperatureFormatted = WeatherFormatter.formatTemperature(avgTemp, unitSystem),
                activities = dayActivities
            )
        }
    }
```

**Step 3: Add `computeVitals` method**

After `computeDayForecasts`, add:

```kotlin
    private fun computeVitals(
        conditions: io.beachforecast.domain.models.CurrentConditions,
        unitSystem: UnitSystem,
        localizedContext: Context
    ): List<VitalState> {
        val uvLevel = when {
            conditions.uvIndex < 3 -> "Low"
            conditions.uvIndex < 6 -> "Moderate"
            conditions.uvIndex < 8 -> "High"
            conditions.uvIndex < 11 -> "Very High"
            else -> "Extreme"
        }
        val uvColor = when {
            conditions.uvIndex < 3 -> "#4CAF50"   // Green
            conditions.uvIndex < 6 -> "#FFB300"   // Amber
            conditions.uvIndex < 8 -> "#FF9800"   // Orange
            conditions.uvIndex < 11 -> "#F44336"  // Red
            else -> "#9C27B0"                      // Purple
        }

        return listOf(
            VitalState(
                label = localizedContext.getString(R.string.widget_vital_wind),
                value = WeatherFormatter.formatWindCompact(
                    conditions.windSpeedKmh, conditions.windDirectionDegrees, unitSystem
                ),
                iconType = "wind"
            ),
            VitalState(
                label = localizedContext.getString(R.string.widget_vital_swell),
                value = WeatherFormatter.formatSwellCompact(
                    conditions.swellHeightMeters, conditions.swellPeriodSeconds, unitSystem
                ),
                iconType = "swell"
            ),
            VitalState(
                label = localizedContext.getString(R.string.widget_vital_sea_temp),
                value = WeatherFormatter.formatSeaTemperature(
                    conditions.seaSurfaceTemperatureCelsius, unitSystem
                ),
                iconType = "sea_temp"
            ),
            VitalState(
                label = localizedContext.getString(R.string.widget_vital_uv),
                value = "%.0f %s".format(conditions.uvIndex, uvLevel),
                iconType = "uv",
                accentColor = uvColor
            )
        )
    }
```

**Step 4: Add `determineBestDay` method**

After `computeVitals`, add:

```kotlin
    private fun determineBestDay(
        dayForecasts: List<DayForecastState>
    ): Int {
        if (dayForecasts.isEmpty()) return -1
        // Best day = most recommended activities + best condition rating
        return dayForecasts.indices.maxByOrNull { index ->
            val day = dayForecasts[index]
            val recommendedCount = day.activities.count { it.isRecommended }
            val ratingScore = when (day.conditionRating) {
                "EPIC" -> 6
                "EXCELLENT" -> 5
                "GOOD" -> 4
                "FAIR" -> 3
                "POOR" -> 2
                "FLAT" -> 1
                else -> 0
            }
            recommendedCount * 10 + ratingScore
        } ?: 0
    }
```

**Step 5: Update `doWork()` to compute new data and pass `selectedSports`**

Replace the success block in `doWork()` (lines 78-107) — from `val recommendations = ...` through `writeSuccessState(...)` — with:

```kotlin
            // Pre-compute activity recommendations for today
            val recommendations = ActivityRecommendationCalculator.calculateForSports(
                conditions, selectedSports
            )
            val activityStates = recommendations.recommendations.map { rec ->
                ActivityState(
                    name = localizedContext.getString(rec.activity.nameRes),
                    activityKey = rec.activity.name,
                    isRecommended = rec.isRecommended,
                    isPrimary = rec.isPrimary,
                    reason = rec.reason ?: ""
                )
            }

            // Pre-compute vitals from current conditions
            val vitals = computeVitals(conditions, unitSystem, localizedContext)

            // Pre-compute 7-day forecasts with per-day activity recommendations
            val weekForecasts = computeDayForecasts(
                weatherData.weekData, unitSystem, localizedContext, languageCode, selectedSports, 7
            )

            // Determine best day
            val bestDayIndex = determineBestDay(weekForecasts)
            val bestDaySummary = if (bestDayIndex >= 0 && bestDayIndex < weekForecasts.size) {
                val best = weekForecasts[bestDayIndex]
                localizedContext.getString(R.string.widget_best_day, best.dayName) +
                    " — ${best.conditionRatingDisplay}  ${best.waveHeightFormatted}  ${best.windFormatted}  ${best.temperatureFormatted}"
            } else ""

            // Write state for all 5 widget types
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
                dayForecastsJson = WidgetStateSerializer.serializeDayForecasts(weekForecasts.take(2)),
                vitalsJson = WidgetStateSerializer.serializeVitals(vitals),
                weekForecastsJson = WidgetStateSerializer.serializeDayForecasts(weekForecasts),
                bestDayIndex = bestDayIndex,
                bestDaySummary = bestDaySummary
            )
```

**Step 6: Update `writeSuccessState()` signature and body**

Replace the `writeSuccessState` method (lines 163-194) with:

```kotlin
    private suspend fun writeSuccessState(
        cityName: String,
        conditionRating: String,
        conditionRatingDisplay: String,
        temperatureFormatted: String,
        waveHeightFormatted: String,
        windFormatted: String,
        activitiesJson: String,
        dayForecastsJson: String,
        vitalsJson: String,
        weekForecastsJson: String,
        bestDayIndex: Int,
        bestDaySummary: String
    ) {
        val manager = GlanceAppWidgetManager(context)
        val widgetInstances = listOf(
            QuickGlanceWidget(),
            TodayForecastWidget(),
            TodayVitalsWidget(),
            TwoDayPlannerWidget(),
            WeekPlannerWidget()
        )

        for (widget in widgetInstances) {
            val glanceIds = manager.getGlanceIds(widget::class.java)
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
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_VITALS_JSON)] = vitalsJson
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_WEEK_FORECASTS_JSON)] = weekForecastsJson
                    prefs[intPreferencesKey(WidgetStateSerializer.KEY_BEST_DAY_INDEX)] = bestDayIndex
                    prefs[stringPreferencesKey(WidgetStateSerializer.KEY_BEST_DAY_SUMMARY)] = bestDaySummary
                    prefs.remove(stringPreferencesKey(WidgetStateSerializer.KEY_ERROR_MESSAGE))
                    prefs[longPreferencesKey(WidgetStateSerializer.KEY_LAST_UPDATED)] = System.currentTimeMillis()
                }
                widget.update(context, glanceId)
            }
        }
    }
```

**Step 7: Update `writeErrorState()` to use new 5 widget types**

Replace `val widgetInstances = listOf(SmallWidget(), MediumWidget(), LargeWidget())` in `writeErrorState()` (line 198) with:

```kotlin
        val widgetInstances = listOf(
            QuickGlanceWidget(),
            TodayForecastWidget(),
            TodayVitalsWidget(),
            TwoDayPlannerWidget(),
            WeekPlannerWidget()
        )
```

**Step 8: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 9: Commit**

```bash
git add app/src/main/java/io/beachforecast/widget/WidgetUpdateWorker.kt
git commit -m "feat(widget): extend worker to compute vitals, weekly forecasts, and per-day activities"
```

---

## Task 11: Update AndroidManifest.xml — replace 3 receivers with 5 (P0)

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`

**Step 1: Replace the 3 receiver blocks (lines 41-75) with 5 new receivers**

Replace everything from `<receiver android:name=".widget.SmallWidgetReceiver"` through the closing `</receiver>` of `LargeWidgetReceiver` with:

```xml
        <receiver
            android:name=".widget.QuickGlanceWidgetReceiver"
            android:exported="true"
            android:label="@string/widget_quick_glance_label">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/widget_quick_glance_info" />
        </receiver>

        <receiver
            android:name=".widget.TodayForecastWidgetReceiver"
            android:exported="true"
            android:label="@string/widget_today_forecast_label">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/widget_today_forecast_info" />
        </receiver>

        <receiver
            android:name=".widget.TodayVitalsWidgetReceiver"
            android:exported="true"
            android:label="@string/widget_today_vitals_label">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/widget_today_vitals_info" />
        </receiver>

        <receiver
            android:name=".widget.TwoDayPlannerWidgetReceiver"
            android:exported="true"
            android:label="@string/widget_two_day_label">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/widget_two_day_info" />
        </receiver>

        <receiver
            android:name=".widget.WeekPlannerWidgetReceiver"
            android:exported="true"
            android:label="@string/widget_week_planner_label">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/widget_week_planner_info" />
        </receiver>
```

**Step 2: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/AndroidManifest.xml
git commit -m "feat(widget): register 5 new widget receivers in manifest"
```

---

## Task 12: Delete old widget files and XML configs (P1)

**Files:**
- Delete: `app/src/main/java/io/beachforecast/widget/SmallWidget.kt`
- Delete: `app/src/main/java/io/beachforecast/widget/MediumWidget.kt`
- Delete: `app/src/main/java/io/beachforecast/widget/LargeWidget.kt`
- Delete: `app/src/main/res/xml/widget_small_info.xml`
- Delete: `app/src/main/res/xml/widget_medium_info.xml`
- Delete: `app/src/main/res/xml/widget_large_info.xml`

Also check if `presentation/binders/` has old binder files to delete.

**Step 1: Verify no remaining references to old widget classes**

Run:
```bash
grep -r "SmallWidget\|MediumWidget\|LargeWidget\|SmallWidgetReceiver\|MediumWidgetReceiver\|LargeWidgetReceiver" app/src/main/ --include="*.kt" --include="*.xml"
```

Expected: Only the files we're about to delete should match. If `WidgetUpdateWorker.kt` still references them, go back and fix Task 10.

**Step 2: Delete old files**

```bash
rm app/src/main/java/io/beachforecast/widget/SmallWidget.kt
rm app/src/main/java/io/beachforecast/widget/MediumWidget.kt
rm app/src/main/java/io/beachforecast/widget/LargeWidget.kt
rm app/src/main/res/xml/widget_small_info.xml
rm app/src/main/res/xml/widget_medium_info.xml
rm app/src/main/res/xml/widget_large_info.xml
```

**Step 3: Check for and delete old binder files**

```bash
ls app/src/main/java/io/beachforecast/presentation/binders/ 2>/dev/null
```

If SmallWidgetBinder.kt, MediumWidgetBinder.kt, LargeWidgetBinder.kt exist, delete them:
```bash
rm app/src/main/java/io/beachforecast/presentation/binders/SmallWidgetBinder.kt
rm app/src/main/java/io/beachforecast/presentation/binders/MediumWidgetBinder.kt
rm app/src/main/java/io/beachforecast/presentation/binders/LargeWidgetBinder.kt
```

**Step 4: Remove old string resources no longer needed**

In `values/strings.xml`, delete lines referencing `widget_small_label`, `widget_small_description`, `widget_medium_label`, `widget_medium_description`, `widget_large_label`, `widget_large_description` — these were replaced in Task 3.

Similarly for `values-he/strings.xml`.

**Step 5: Verify build**

Run: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```bash
git add -A
git commit -m "refactor(widget): delete old Small/Medium/Large widget files and XML configs"
```

---

## Task 13: Run full test suite and fix any failures (P0)

**Step 1: Run tests**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew test --console=plain 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL. If tests fail, read the failure output and fix the failing test or code.

**Step 2: Check for compile warnings**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew assembleDebug --console=plain 2>&1 | grep -i "warning\|error" | head -20
```

Fix any new warnings introduced by our changes.

**Step 3: Commit any fixes**

```bash
git add -A
git commit -m "fix(widget): address test failures and warnings from widget redesign"
```

---

## Task 14: Final verification — full build + test (P0)

**Step 1: Clean build**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew clean assembleDebug --console=plain 2>&1 | tail -5
```

Expected: BUILD SUCCESSFUL

**Step 2: Run full test suite**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && ./gradlew test --console=plain 2>&1 | tail -10
```

Expected: BUILD SUCCESSFUL

**Step 3: Verify no references to deleted files**

```bash
grep -r "SmallWidget\|MediumWidget\|LargeWidget" app/src/ --include="*.kt" --include="*.xml" | grep -v "test/"
```

Expected: No output (all references cleaned up).
