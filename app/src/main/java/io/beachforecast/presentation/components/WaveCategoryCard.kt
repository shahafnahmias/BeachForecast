package io.beachforecast.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.beachforecast.R
import io.beachforecast.domain.models.UnitConverter
import io.beachforecast.domain.models.UnitSystem
import io.beachforecast.domain.models.WaveCategory
import io.beachforecast.ui.theme.*

/**
 * Card displaying wave height in CM with color coding
 */
@Composable
fun WaveCategoryCard(
    waveCategory: WaveCategory,
    waveHeight: Double,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") showImage: Boolean = true,
    isCompact: Boolean = false,
    unitSystem: UnitSystem = UnitSystem.METRIC
) {
    val context = LocalContext.current
    val categoryColor = getCategoryColor(waveCategory)
    val gradientColors = listOf(
        categoryColor.copy(alpha = 0.4f),
        categoryColor.copy(alpha = 0.15f),
        categoryColor.copy(alpha = 0.05f)
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = categoryColor.copy(alpha = 0.6f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(gradientColors)
                )
                .padding(if (isCompact) 12.dp else 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Wave height as range with color coding
            Text(
                text = UnitConverter.formatWaveHeightRange(waveHeight, unitSystem),
                style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                color = categoryColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getCategoryColor(category: WaveCategory): Color {
    return when (category) {
        WaveCategory.FLAT -> WaveFlat
        WaveCategory.ANKLE -> WaveAnkle
        WaveCategory.KNEE -> WaveKnee
        WaveCategory.WAIST -> WaveWaist
        WaveCategory.CHEST -> WaveChest
        WaveCategory.HEAD_HIGH -> WaveHeadHigh
        WaveCategory.OVERHEAD -> WaveOverhead
        WaveCategory.DOUBLE_OVERHEAD -> WaveDoubleOverhead
        WaveCategory.TRIPLE_OVERHEAD_PLUS -> WaveTriplePlus
    }
}
