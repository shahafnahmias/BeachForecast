# Widget Redesign v2 — Activity-Rich Widgets

**Date:** 2026-02-26
**Status:** Approved

## Overview

Replace the existing 3 widgets (Small 2x2, Medium 4x2, Large 4x3) with 5 new purpose-driven widgets that bring the app's activity recommendations, vitals, and forecasting into the widget experience.

## Strategy

- **Replace** all 3 existing Glance widgets with 5 new ones
- Sport icons via **XML vector drawables** (same Material icons as app)
- Theme follows **app setting** (dark/light/system)
- Show only **user-selected sports** from app preferences
- All data pre-computed in `WidgetUpdateWorker` (widgets are dumb renderers)

## Widget Catalog

### Widget 1: Quick Glance (2x2)

**Purpose:** "Should I go to the beach right now?"

```
┌─────────────────────┐
│ 🏖 Herzliya    25°  │
│                     │
│   🏊  🏄  🪁  🏄‍♂️  │
│   ●   ●   ○   ○   │
│                     │
│  ▌ Good Conditions ▐│
└─────────────────────┘
```

**Elements:**
- Header: App icon + beach name + temperature
- Center: User-selected sport icons in circular containers (green-tinted if recommended, gray/dim if not)
- Footer: Condition rating text with colored background bar (ConditionColors palette)
- Tap: Opens app

### Widget 2: Today's Forecast (4x2)

**Purpose:** "What sports are good today and why?"

```
┌──────────────────────────────────────────┐
│ 🏖 Herzliya              Good · 25°     │
├──────────────────────────────────────────┤
│  [🏊]        │  [🏄]        │  [🪁]     │
│  Swim        │  Surf        │  Kite     │
│  ● Calm sea  │  ● Good swell│  ○ No wind│
└──────────────────────────────────────────┘
```

**Elements:**
- Header: Beach name + condition badge (colored) + temperature
- Body: Equal-width sport cards (2-4 based on user selection). Each card has sport icon in tinted circle, sport name (bold), recommendation dot + reason text
- Tap: Opens app to home screen

### Widget 3: Today + Vitals (4x3)

**Purpose:** "Full picture for today"

```
┌──────────────────────────────────────────┐
│ 🏖 Herzliya              Good · 25°     │
├──────────────────────────────────────────┤
│  [🏊]        │  [🏄]        │  [🪁]     │
│  Swim        │  Surf        │  Kite     │
│  ● Calm sea  │  ● Good swell│  ○ No wind│
├──────────────────────────────────────────┤
│  💨 12 km/h NW  │  🌊 1.2m · 8s        │
│  🌡 Sea 18°C    │  ☀️ UV 6 Moderate     │
└──────────────────────────────────────────┘
```

**Elements:**
- Top half: Identical to Widget 2 (header + sports row)
- Bottom half: 2x2 vitals grid — Wind (speed + direction), Swell (height + period), Sea temperature, UV index (with severity color)
- Tap: Opens app

### Widget 4: Two-Day Planner (4x3)

**Purpose:** "Today vs tomorrow — when should I go?"

```
┌──────────────────────────────────────────┐
│ 🏖 Herzliya                              │
├────────────────────┬─────────────────────┤
│  TODAY             │  TOMORROW           │
│  ▌Good▐  25°      │  ▌Fair▐  23°        │
│                    │                     │
│  🏊● 🏄● 🪁○     │  🏊● 🏄○ 🪁●      │
│                    │                     │
│  🌊 0.8m  💨 12   │  🌊 0.5m  💨 20    │
└────────────────────┴─────────────────────┘
```

**Elements:**
- Header: Beach name spanning full width
- Two equal columns side-by-side, each day showing: day label, condition badge (colored) + temperature, sport icons with ●/○ indicators, wave height + wind speed
- "Today" column has subtle highlight background
- Tap: Opens app to forecast screen

### Widget 5: Week Planner (4x4)

**Purpose:** "Which day should I plan for?"

```
┌──────────────────────────────────────────┐
│ 🏖 Herzliya — Week Forecast             │
├──────────────────────────────────────────┤
│       Mon  Tue  Wed  Thu  Fri  Sat  Sun  │
│ 🏄    ○    ●    ●    ○    ○    ●    ●   │
│ 🏊    ●    ●    ○    ●    ●    ●    ○   │
│ 🪁    ○    ○    ●    ●    ○    ○    ●   │
├──────────────────────────────────────────┤
│  ★ Best: Saturday — Good Conditions     │
│     🌊 1.0m  💨 15 km/h  🌡 26°        │
└──────────────────────────────────────────┘
```

**Elements:**
- Header: Beach name + "Week Forecast"
- Grid: Days (columns) x user-selected sports (rows). Green ● = recommended, gray ○ = not. Best day column highlighted.
- Footer: Best overall day callout with condition rating + key metrics (wave, wind, temp)
- Tap: Opens app to forecast screen

## Data Architecture

### WidgetUpdateWorker Changes

The worker currently computes data for 3 widget types. It needs to be extended to:

1. **Read user-selected sports** from `UserPreferences`
2. **Compute activity recommendations for each day** (today, tomorrow, and 5 more days) using `ActivityRecommendationCalculator`
3. **Extract vitals** from current weather data (wind, swell, sea temp, UV)
4. **Determine best day** across the week for each sport and overall
5. **Serialize per-widget-type state** into DataStore

### New WidgetState Structures

```kotlin
// Shared
data class SportState(
    val name: String,           // Localized display name
    val iconRes: String,        // Drawable resource name
    val isRecommended: Boolean,
    val reason: String          // Localized reason text
)

data class DaySnapshot(
    val dayName: String,                // "Today", "Tomorrow", "Wednesday", etc.
    val conditionRating: String,        // ConditionRating enum name
    val conditionRatingDisplay: String, // Localized text
    val temperature: String,            // "25°"
    val sports: List<SportState>,
    val waveHeight: String,             // "0.8m"
    val windSpeed: String               // "12 km/h"
)

data class VitalState(
    val label: String,      // "Wind", "Swell", etc.
    val value: String,      // "12 km/h NW"
    val iconRes: String,    // Drawable resource name
    val accentColor: String? // Optional severity color (for UV)
)

// Per-widget states
data class QuickGlanceState(
    val cityName: String,
    val temperature: String,
    val conditionRating: String,
    val conditionRatingDisplay: String,
    val sports: List<SportState>
)

data class TodayForecastState(
    val cityName: String,
    val temperature: String,
    val conditionRating: String,
    val conditionRatingDisplay: String,
    val sports: List<SportState>
)

data class TodayVitalsState(
    val cityName: String,
    val temperature: String,
    val conditionRating: String,
    val conditionRatingDisplay: String,
    val sports: List<SportState>,
    val vitals: List<VitalState>
)

data class TwoDayState(
    val cityName: String,
    val today: DaySnapshot,
    val tomorrow: DaySnapshot
)

data class WeekPlannerState(
    val cityName: String,
    val days: List<DaySnapshot>,        // 7 days
    val bestDayIndex: Int,              // Index into days list
    val bestDayCondition: String,       // Localized condition text
    val bestDayWave: String,
    val bestDayWind: String,
    val bestDayTemp: String
)
```

### Icon Assets (4 vector drawables)

- `ic_sport_swim.xml` — Material: Pool (outlined)
- `ic_sport_surf.xml` — Material: Surfing (outlined)
- `ic_sport_kite.xml` — Material: Air (outlined)
- `ic_sport_sup.xml` — Material: Kayaking (outlined)

These are imported from the Material Design icon set as Android XML vector drawables. Tinted at render time via Glance `ColorFilter`.

### Theme

- Read app theme preference from `UserPreferences`
- Widget background: Stitch dark (`#101F22`) or light (`#F5F8F8`)
- Surface cards: `#1A2E32` (dark) or `#FFFFFF` (light)
- Text: white (dark) or dark gray (light)
- Condition colors: Shared `ConditionColors` palette (green→amber→red→gray)
- Sport icon tint: Green (`#4CAF50`) if recommended, gray (`#78909C`) if not

### Localization

All display strings pre-localized in the worker (English + Hebrew). Sport names, condition ratings, reason text, day names, and vital labels all use string resources resolved at worker execution time.

## Files to Create/Modify

### New Files
- `widget/QuickGlanceWidget.kt` — 2x2 Glance widget
- `widget/TodayForecastWidget.kt` — 4x2 Glance widget
- `widget/TodayVitalsWidget.kt` — 4x3 Glance widget
- `widget/TwoDayPlannerWidget.kt` — 4x3 Glance widget
- `widget/WeekPlannerWidget.kt` — 4x4 Glance widget
- `widget/state/` — New state data classes
- `widget/components/` — Shared Glance composable components (sport icon row, condition badge, vital card, etc.)
- `res/drawable/ic_sport_swim.xml`, `ic_sport_surf.xml`, `ic_sport_kite.xml`, `ic_sport_sup.xml`
- `res/xml/` — 5 new widget info XML files (replacing 3 old ones)

### Modified Files
- `widget/WidgetUpdateWorker.kt` — Extended data computation
- `widget/WidgetUpdateHelper.kt` — Register 5 widget types
- `AndroidManifest.xml` — Replace 3 widget receivers with 5 new ones
- `res/values/strings.xml` + `res/values-iw/strings.xml` — New widget-related strings

### Deleted Files
- `widget/SmallWidget.kt`
- `widget/MediumWidget.kt`
- `widget/LargeWidget.kt`
- `widget/BaseWidgetProvider.kt` (if no longer needed)
- `presentation/binders/SmallWidgetBinder.kt`, `MediumWidgetBinder.kt`, `LargeWidgetBinder.kt`
- Old widget info XMLs

## Technical Constraints

- **Glance composables:** Box, Column, Row, Spacer, Text, Image, LazyColumn (limited). No animations, no custom drawing.
- **ImageProvider:** `ImageProvider(R.drawable.ic_sport_swim)` for vector drawables. Supports `ColorFilter.tint()`.
- **Widget sizes:** Android allocates cells approximately 57dp each. 2x2=~114x114dp, 4x2=~250x114dp, 4x3=~250x171dp, 4x4=~250x228dp. Layouts must be flexible.
- **Background updates:** WorkManager with existing periodic schedule. No change needed to update frequency.
