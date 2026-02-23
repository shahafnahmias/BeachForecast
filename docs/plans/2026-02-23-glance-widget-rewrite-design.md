# Glance Widget Rewrite Design

## Overview

Rewrite all 3 Beach Forecast widgets from RemoteViews + XML to Jetpack Glance with Material You dynamic colors. Each widget serves a distinct purpose rather than being small/medium/large versions of the same data.

## Widget Purposes

### Small (2x2) — Go/No-Go Signal

Instant visual answer to "Should I go to the beach?"

- Hero element: Large condition rating text (EPIC/GOOD/FAIR/POOR/FLAT), 24sp bold, colored by condition
- Supporting: App icon + beach name (14sp) + temperature (14sp)
- Condition color bar: 4dp tall, full width, at bottom
- Background: `widgetBackground` with subtle 10% condition-color tint overlay

No wave height, no wind, no activities. Just a traffic-light-style signal.

### Medium (4x2) — Activity Recommendations

Shows which activities are good right now and why.

- Header: App icon + beach name + condition rating badge
- Body: Adaptive grid of activity cards (filtered to user's selected sports)
  - 2 sports: 1x2 layout
  - 3 sports: 1x3 layout
  - 4 sports: 2x2 grid
- Each activity card shows:
  - Activity name + icon
  - Colored dot (green = recommended, muted = not)
  - One-line reason text

### Large (4x3) — Multi-Day Outlook

Today + 2 days forecast for trip planning.

- Header: App icon + beach name + current temperature
- 3 day rows, each containing:
  - Day name (label)
  - Condition color bar (4dp, full width, colored by that day's ConditionRating)
  - Condition rating text (bold, condition color)
  - Three metrics inline: wave height, wind speed + direction arrow, temperature
- Today row gets slightly more visual prominence (bolder or different container color)

Data source: `WeatherData.weekData` grouped by day, averages calculated in worker.

## Visual Design System

### Theming — Material You Native

Dynamic Colors (Android 12+):
- Widget background: `GlanceTheme.colors.widgetBackground`
- Card surfaces: `GlanceTheme.colors.surfaceVariant`
- Primary text: `GlanceTheme.colors.onSurface`
- Secondary text: `GlanceTheme.colors.onSurfaceVariant`
- Accent: `GlanceTheme.colors.primary`

Fallback (Android < 12):
- Dark: `#101F22` background, `#1A2E32` cards, `#FFFFFF` text
- Light: `#F5F8F8` background, `#FFFFFF` cards, `#101F22` text
- Follows system dark/light mode

Condition colors stay hardcoded (semantic, not decorative):
- EPIC: `#00E676`, EXCELLENT: `#4CAF50`, GOOD: `#66BB6A`
- FAIR: `#FFB300`, POOR: `#FF9800`, FLAT: `#78909C`, DANGEROUS: `#F44336`

### Layout Principles

- 16dp outer padding on all widgets
- 4dp grid for all internal spacing (4, 8, 12, 16dp)
- System corner radius via `system_app_widget_background_radius` (outer) and `system_app_widget_inner_radius` (inner cards)
- `@android:id/background` on root for smooth launch transitions

### Typography

| Role | Size | Weight | Color |
|---|---|---|---|
| Hero (condition rating) | 24-28sp | Bold | Condition color |
| Beach name | 14sp | Medium | onSurface |
| Data value | 14sp | Bold | onSurface |
| Label (day name) | 11sp | Medium | onSurfaceVariant |
| Supporting (reason) | 11sp | Normal | onSurfaceVariant |

## Architecture

### File Structure

```
widget/
  SmallWidget.kt          — GlanceAppWidget + Receiver + Composable content
  MediumWidget.kt         — GlanceAppWidget + Receiver + Composable content
  LargeWidget.kt          — GlanceAppWidget + Receiver + Composable content
  WidgetState.kt          — Serializable state data classes
  WidgetUpdateWorker.kt   — Refactored: fetches data, serializes to GlanceState
  WidgetUpdateHelper.kt   — Unchanged
  GenerateTodaySummaryUseCase.kt — Kept (unused by new widgets)
  theme/
    WidgetTheme.kt        — GlanceTheme wrapper with condition colors
```

### State Management

1. WidgetUpdateWorker fetches weather data (same flow as today)
2. Worker pre-computes all display data (activity recommendations, daily averages, condition ratings)
3. Worker serializes result to DataStore-backed GlanceState (Preferences keys)
4. Worker calls `SmallWidget().updateAll(context)` etc. to trigger re-composition
5. Each widget's `provideGlance()` reads state and renders

Widget state (serialized to Preferences):
- cityName, conditionRating, waveHeightMeters, temperatureCelsius
- windSpeedKmh, windDirectionDegrees, uvIndex
- swellHeightMeters, swellPeriodSeconds, seaSurfaceTemperatureCelsius
- activityRecommendations: JSON list of {activity, isRecommended, reason}
- dayForecasts: JSON list of {dayName, conditionRating, avgWaveHeight, avgWindSpeed, avgTemp}
- lastUpdated, unitSystem, languageCode

### Data Flow

```
System/User triggers update
  → WidgetUpdateHelper.enqueueUpdate()
  → WidgetUpdateWorker.doWork()
    → GetWidgetDataUseCase.execute()
    → ActivityRecommendationCalculator.calculateForSports()
    → Daily averaging from weekData (3 days)
    → Serialize to GlanceState
    → SmallWidget/MediumWidget/LargeWidget.updateAll()
  → Glance re-invokes provideGlance()
    → Reads state from Preferences
    → Renders Composable UI
```

### Click Handling

`actionStartActivity<MainActivity>()` on root container. Replaces manual PendingIntent setup.

### RTL / Localization

Glance Compose handles RTL automatically. Worker still uses `updateContextLocale()` for string resource resolution during pre-computation.

### Error State

All sizes show: app icon + "Beach Forecast" + error message + "Tap to retry". Stored as a flag in GlanceState.

### Loading State

On first-ever render with no state: app icon + "Beach Forecast" + "Loading...". Subsequent updates show last good state until new data arrives.

## Files to Delete

- `BaseWidgetProvider.kt` — replaced by GlanceAppWidgetReceiver
- `SmallWidgetProvider.kt` — merged into SmallWidget.kt
- `MediumWidgetProvider.kt` — merged into MediumWidget.kt
- `LargeWidgetProvider.kt` — merged into LargeWidget.kt
- `SmallWidgetBinder.kt` — replaced by Composable
- `MediumWidgetBinder.kt` — replaced by Composable
- `LargeWidgetBinder.kt` — replaced by Composable
- `WidgetThemeColors.kt` — replaced by WidgetTheme.kt
- `widget_small.xml` — replaced by Composable
- `widget_medium.xml` — replaced by Composable
- `widget_large.xml` — replaced by Composable
- `rounded_card_*.xml` (7 files) — styling via Glance modifiers

## Files to Modify

- `app/build.gradle.kts` — add Glance dependencies
- `gradle/libs.versions.toml` — add Glance version
- `AndroidManifest.xml` — update receiver class names
- `widget_small_info.xml` — remove initialLayout/previewLayout
- `widget_medium_info.xml` — remove initialLayout/previewLayout
- `widget_large_info.xml` — remove initialLayout/previewLayout

## Files Unchanged

- `WidgetUpdateHelper.kt` — still enqueues WorkManager
- `GenerateTodaySummaryUseCase.kt` + test — kept
- All domain models, calculators, formatters, repositories

## Dependencies to Add

```toml
# libs.versions.toml
glance = "1.1.1"  # or latest stable

[libraries]
androidx-glance-appwidget = { group = "androidx.glance", name = "glance-appwidget", version.ref = "glance" }
```
