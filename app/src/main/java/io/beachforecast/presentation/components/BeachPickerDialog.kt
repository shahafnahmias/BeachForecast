package io.beachforecast.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.beachforecast.R
import io.beachforecast.domain.models.Beach
import io.beachforecast.domain.models.BeachSelection


@Composable
fun BeachPickerDialog(
    currentSelection: BeachSelection,
    beaches: List<Beach>,
    nearestBeachId: String?,
    languageCode: String,
    onBeachSelected: (BeachSelection) -> Unit,
    onDismiss: () -> Unit
) {
    // Resolve the effective selected beach ID for display
    val selectedBeachId = when (currentSelection) {
        is BeachSelection.Auto -> nearestBeachId
        is BeachSelection.Manual -> currentSelection.beachId
    }

    // Sort beaches: nearest first, then the rest in original order
    val sortedBeaches = remember(beaches, nearestBeachId) {
        if (nearestBeachId != null) {
            val nearest = beaches.find { it.id == nearestBeachId }
            val rest = beaches.filter { it.id != nearestBeachId }
            if (nearest != null) listOf(nearest) + rest else beaches
        } else {
            beaches
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.beach_picker_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedBeaches, key = { it.id }) { beach ->
                        val isNearest = beach.id == nearestBeachId
                        BeachOption(
                            title = beach.getLocalizedName(languageCode),
                            subtitle = if (isNearest) stringResource(R.string.beach_nearest_to_you) else null,
                            isSelected = beach.id == selectedBeachId,
                            onClick = {
                                // Nearest beach → Auto (tracks location changes)
                                // Other beach → Manual
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

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.language_cancel))
                }
            }
        }
    }
}

@Composable
fun BeachOption(
    title: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
