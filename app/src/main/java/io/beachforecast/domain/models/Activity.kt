package io.beachforecast.domain.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import io.beachforecast.R

/**
 * Water activities that can be recommended based on conditions.
 */
enum class Activity(
    val displayName: String,
    val iconName: String, // Material icon name
    @StringRes val nameRes: Int
) {
    SWIM("Swim", "pool", R.string.activity_swim),
    SURF("Surf", "surfing", R.string.activity_surf),
    KITE("Kite", "air", R.string.activity_kite),
    SUP("SUP", "kayaking", R.string.activity_sap); // Stand-up Paddleboard

    companion object {
        val all = entries.toList()

        fun getDefaults(): Set<Activity> = entries.toSet()
    }
}

/**
 * Recommendation for a specific activity based on current conditions.
 */
@Stable
data class ActivityRecommendation(
    val activity: Activity,
    val isRecommended: Boolean,
    val isPrimary: Boolean = false, // The highlighted/best activity
    val reason: String? = null
)

/**
 * Overall condition rating for the current conditions.
 */
enum class ConditionRating(
    val displayName: String,
    @StringRes val nameRes: Int
) {
    EPIC("Epic Conditions", R.string.condition_epic),
    EXCELLENT("Excellent", R.string.condition_excellent),
    GOOD("Good Conditions", R.string.condition_good),
    FAIR("Fair Conditions", R.string.condition_fair),
    POOR("Poor Conditions", R.string.condition_poor),
    FLAT("Flat", R.string.condition_flat),
    DANGEROUS("Dangerous", R.string.condition_dangerous);

    companion object {
        fun forSurf(waveHeight: Double): ConditionRating {
            return when {
                waveHeight >= 2.0 -> EPIC
                waveHeight >= 1.5 -> EXCELLENT
                waveHeight >= 1.0 -> GOOD
                waveHeight >= 0.5 -> FAIR
                waveHeight >= 0.3 -> POOR
                else -> FLAT
            }
        }
    }
}

/**
 * Widget color for condition rating indicator.
 * Returns ARGB int color suitable for RemoteViews text coloring.
 */
fun ConditionRating.getWidgetColor(): Int = when (this) {
    ConditionRating.EPIC -> 0xFF00E676.toInt()         // bright green
    ConditionRating.EXCELLENT -> 0xFF4CAF50.toInt()     // green
    ConditionRating.GOOD -> 0xFF66BB6A.toInt()          // lighter green
    ConditionRating.FAIR -> 0xFFFFB300.toInt()           // amber
    ConditionRating.POOR -> 0xFFFF9800.toInt()           // orange
    ConditionRating.FLAT -> 0xFF78909C.toInt()           // blue-grey
    ConditionRating.DANGEROUS -> 0xFFF44336.toInt()      // red
}

/**
 * Result of activity recommendation calculation.
 */
@Stable
data class ActivityRecommendations(
    val recommendations: List<ActivityRecommendation>,
    val conditionRating: ConditionRating,
    val primaryActivity: Activity?
) {
    val primaryRecommendation: ActivityRecommendation?
        get() = recommendations.find { it.isPrimary }

    fun isRecommended(activity: Activity): Boolean {
        return recommendations.find { it.activity == activity }?.isRecommended ?: false
    }

    fun filterByActivities(selectedActivities: Set<Activity>): ActivityRecommendations {
        if (selectedActivities.isEmpty()) {
            return this
        }

        val filteredRecommendations = recommendations.filter { selectedActivities.contains(it.activity) }
        val recalculatedPrimary = when {
            filteredRecommendations.isEmpty() -> null
            primaryActivity != null && filteredRecommendations.any { it.activity == primaryActivity } -> primaryActivity
            filteredRecommendations.any { it.isRecommended } -> {
                filteredRecommendations.firstOrNull { it.isRecommended }?.activity
            }
            else -> null
        }

        return ActivityRecommendations(
            recommendations = filteredRecommendations.map { rec ->
                rec.copy(isPrimary = rec.activity == recalculatedPrimary)
            },
            conditionRating = conditionRating,
            primaryActivity = recalculatedPrimary
        )
    }
}
