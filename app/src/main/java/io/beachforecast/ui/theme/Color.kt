package io.beachforecast.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Stitch Design System Colors
// ==========================================

// Primary
val StitchPrimary = Color(0xFF0DCCF2)           // Cyan - main accent color
val StitchPrimaryDark = Color(0xFF0AB8DB)       // Darker cyan for pressed states
val StitchPrimaryLight = Color(0xFF4DDBF7)      // Lighter cyan

// Dark Theme Backgrounds
val StitchBackgroundDark = Color(0xFF101F22)    // Main background (navy)
val StitchSurfaceDark = Color(0xFF1A2E32)       // Card/surface background
val StitchSurfaceVariantDark = Color(0xFF243B40) // Elevated surfaces

// Dark Theme Text
val StitchOnBackgroundDark = Color(0xFFFFFFFF)  // Primary text (white)
val StitchOnSurfaceDark = Color(0xFFFFFFFF)     // Text on surfaces
val StitchTextSecondary = Color(0xFF90C1CB)     // Secondary/muted text (cyan-gray)
val StitchTextTertiary = Color(0xFF5A7A82)      // Tertiary text

// Light Theme Backgrounds
val StitchBackgroundLight = Color(0xFFF5F8F8)   // Main background
val StitchSurfaceLight = Color(0xFFFFFFFF)      // Card/surface background
val StitchSurfaceVariantLight = Color(0xFFE8EEEF) // Elevated surfaces

// Light Theme Text
val StitchOnBackgroundLight = Color(0xFF101F22) // Primary text (dark)
val StitchOnSurfaceLight = Color(0xFF101F22)    // Text on surfaces
val StitchTextSecondaryLight = Color(0xFF5A7A82) // Secondary/muted text

// Glass Card Effects
val StitchGlassBackground = Color(0x08FFFFFF)   // 3% white
val StitchGlassBorder = Color(0x14FFFFFF)       // 8% white
val StitchActiveCardBackground = Color(0x1A0DCCF2) // 10% primary
val StitchActiveCardBorder = Color(0x4D0DCCF2)  // 30% primary

// Status Colors
val StitchAmber = Color(0xFFF59E0B)             // UV/warning indicators
val StitchEmerald = Color(0xFF10B981)           // Positive/safe conditions
val StitchRed = Color(0xFFEF4444)               // Danger/extreme conditions
val StitchSlate = Color(0xFF64748B)             // Inactive/disabled

// ==========================================
// Legacy Ocean-inspired colors (kept for backwards compatibility)
// ==========================================
val DeepSpaceBlue = Color(0xFF0A0E27)
val DarkNavy = Color(0xFF0F1629)
val MidnightBlue = Color(0xFF1A1F3A)
val PrimaryCyan = Color(0xFF00D9FF)
val LightCyan = Color(0xFF66E7FF)
val DarkCyan = Color(0xFF00A8CC)
val AccentTeal = Color(0xFF00C9B7)
val OffWhite = Color(0xFFE8F1FF)
val MutedGray = Color(0xFF8B95B0)
val LightGray = Color(0xFFB8C5E0)
val ErrorRed = Color(0xFFFF5555)
val WarningOrange = Color(0xFFFFAA33)
val SuccessGreen = Color(0xFF00E676)

// Wave category colors - Enhanced for better vibrancy and differentiation
val WaveFlat = Color(0xFF5BA3F5)        // Brighter blue
val WaveAnkle = Color(0xFF00E5FF)      // More vibrant cyan
val WaveKnee = Color(0xFF00D4C1)       // Brighter teal
val WaveWaist = Color(0xFF00C896)      // More vibrant green
val WaveChest = Color(0xFFFFB74D)      // Brighter orange
val WaveHeadHigh = Color(0xFFFF8A65)    // More vibrant orange-red
val WaveOverhead = Color(0xFFFF6B6B)    // Brighter red
val WaveDoubleOverhead = Color(0xFFF06292)  // Brighter pink
val WaveTriplePlus = Color(0xFFC2185B)  // More vibrant deep pink

// Condition severity colors (for wind, swell intensity)
object ConditionColors {
    // Green spectrum - Calm/Safe
    val Calm = Color(0xFF4CAF50)
    val Light = Color(0xFF8BC34A)

    // Yellow/Orange spectrum - Moderate
    val Moderate = Color(0xFFFFEB3B)
    val Fresh = Color(0xFFFF9800)

    // Red spectrum - Strong/Dangerous
    val Strong = Color(0xFFFF5722)
    val VeryStrong = Color(0xFFF44336)
}