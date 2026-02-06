package io.beachforecast.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.beachforecast.ui.theme.StitchTheme

/**
 * Glass card component matching the Stitch design system.
 * Semi-transparent background with subtle border.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = StitchTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.glassBackground)
            .border(
                width = 1.dp,
                color = colors.glassBorder,
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp),
        content = content
    )
}

/**
 * Glass card variant with no default padding.
 */
@Composable
fun GlassCardNoPadding(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = StitchTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.glassBackground)
            .border(
                width = 1.dp,
                color = colors.glassBorder,
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

/**
 * Active/highlighted card with cyan border and glow effect.
 * Used for currently selected items like "NOW" hour card.
 */
@Composable
fun ActiveCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    glowIntensity: Float = 0.3f,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = StitchTheme.colors
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .drawBehind {
                // Glow effect - draw larger colored rectangle behind
                drawRoundRect(
                    color = primaryColor.copy(alpha = glowIntensity * 0.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
                )
            }
            .background(colors.activeCardBackground)
            .border(
                width = 1.dp,
                color = colors.activeCardBorder,
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp),
        content = content
    )
}

/**
 * Active card variant with no default padding.
 */
@Composable
fun ActiveCardNoPadding(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    glowIntensity: Float = 0.3f,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = StitchTheme.colors
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .drawBehind {
                drawRoundRect(
                    color = primaryColor.copy(alpha = glowIntensity * 0.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
                )
            }
            .background(colors.activeCardBackground)
            .border(
                width = 1.dp,
                color = colors.activeCardBorder,
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

/**
 * Pulsing dot animation for live status indicators.
 * Matches the "5m ago" live status in the Stitch design.
 */
@Composable
fun PulsingDot(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing outer ring
        Box(
            modifier = Modifier
                .size(size)
                .scale(scale)
                .graphicsLayer { this.alpha = alpha }
                .background(color, CircleShape)
        )
        // Static inner dot
        Box(
            modifier = Modifier
                .size(size)
                .background(color, CircleShape)
        )
    }
}

/**
 * Live status badge component with pulsing dot.
 * Shows time since last update (e.g., "5m ago").
 */
@Composable
fun LiveStatusBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = StitchTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(colors.glassBackground)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PulsingDot(size = 8.dp)
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Section header matching Stitch design.
 * Title on left, optional action button on right.
 */
@Composable
fun StitchSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Condition badge (e.g., "EXCELLENT SURF", "FAIR CONDITIONS").
 */
@Composable
fun ConditionBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
    textColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}
