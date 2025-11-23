package com.example.naifdeneme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naifdeneme.database.DrinkType

/**
 * İçecek Tipi Seçici Component
 * Horizontal scrollable list of drink types
 */
@Composable
fun DrinkTypeSelector(
    selectedType: DrinkType,
    onTypeSelected: (DrinkType) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val neonCyan = Color(0xFF06F9F9)

    Column(modifier = modifier) {
        Text(
            text = stringResource(com.example.naifdeneme.R.string.select_drink_type),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(DrinkType.values()) { drinkType ->
                DrinkTypeChip(
                    drinkType = drinkType,
                    isSelected = selectedType == drinkType,
                    onClick = {
                        if (enabled) onTypeSelected(drinkType)
                    },
                    enabled = enabled,
                    selectedColor = neonCyan
                )
            }
        }
    }
}

/**
 * Tek bir içecek tipi chip'i
 */
@Composable
private fun DrinkTypeChip(
    drinkType: DrinkType,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    selectedColor: Color
) {
    val backgroundColor = when {
        !enabled -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        isSelected -> selectedColor.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        isSelected -> selectedColor
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    val textColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        isSelected -> selectedColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .width(100.dp)
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emoji
            Text(
                text = drinkType.emoji,
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // İsim
            Text(
                text = stringResource(drinkType.nameResId),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * Basit seçili içecek gösterimi (compact view)
 */
@Composable
fun SelectedDrinkDisplay(
    drinkType: DrinkType,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = drinkType.emoji,
            fontSize = 20.sp
        )
        Text(
            text = stringResource(drinkType.nameResId),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// ============================================
// PREVIEW
// ============================================

@Preview(showBackground = true)
@Composable
fun DrinkTypeSelectorPreview() {
    var selectedType by remember { mutableStateOf(DrinkType.WATER) }

    com.example.naifdeneme.ui.theme.ModaiTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            DrinkTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SelectedDrinkDisplay(drinkType = selectedType)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrinkTypeSelectorDisabledPreview() {
    com.example.naifdeneme.ui.theme.ModaiTheme {
        DrinkTypeSelector(
            selectedType = DrinkType.COFFEE,
            onTypeSelected = {},
            enabled = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}