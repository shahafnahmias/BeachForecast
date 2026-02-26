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
            text = "Loading\u2026",
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
