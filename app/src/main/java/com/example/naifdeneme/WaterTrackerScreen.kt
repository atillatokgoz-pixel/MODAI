package com.example.naifdeneme.ui.screens.water

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.naifdeneme.R
import com.example.naifdeneme.ui.theme.ModaiTheme

/**
 * Su İçimi Ana Ekran - HTML Tasarımına Göre Güncellendi
 * Neon cyan tema (#06F9F9) ve modern layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    // State
    var currentAmount by remember { mutableStateOf(1500) }
    val targetAmount = 2500
    var selectedAmount by remember { mutableStateOf(500) }
    var isReminderEnabled by remember { mutableStateOf(false) }

    val progress = currentAmount.toFloat() / targetAmount.toFloat()
    val neonCyan = Color(0xFF06F9F9)

    Scaffold(
        topBar = {
            WaterTrackerTopBar(
                onNavigateBack = onNavigateBack,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Ring Section
            Column(
                modifier = Modifier.padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Progress
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background Circle
                    CircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.size(200.dp),
                        strokeWidth = 8.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    // Progress Circle with Glow Effect
                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .size(200.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                spotColor = neonCyan.copy(alpha = 0.5f)
                            ),
                        strokeWidth = 8.dp,
                        color = neonCyan
                    )

                    // Text Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentAmount.toString(),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "/ $targetAmount ml",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Reminder Card
            ReminderCard(
                isEnabled = isReminderEnabled,
                onToggle = { isReminderEnabled = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Amount Selector (Segmented Control)
            AmountSelector(
                selectedAmount = selectedAmount,
                onAmountSelected = { selectedAmount = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Add Water Button
            AddWaterButton(
                amount = selectedAmount,
                onClick = {
                    currentAmount += selectedAmount
                    if (currentAmount > targetAmount) currentAmount = targetAmount
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Daily Statistics
            DailyStatsSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Activities
            RecentActivitiesSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Top App Bar - Güncellendi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerTopBar(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.water_tracker_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_revert),
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        actions = {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

/**
 * Reminder Card - Güncellendi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val neonCyan = Color(0xFF06F9F9)

    Card(
        onClick = { /* Navigate to reminder settings */ },
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(neonCyan.copy(alpha = 0.2f))
                        .shadow(
                            elevation = 2.dp,
                            shape = CircleShape,
                            spotColor = neonCyan.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = neonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.reminders),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isEnabled) stringResource(R.string.reminder_on) else stringResource(R.string.reminder_off),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = neonCyan,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            )
        }
    }
}

/**
 * Amount Selector (Segmented Control) - Basit ve temiz versiyon
 */
@Composable
fun AmountSelector(
    selectedAmount: Int,
    onAmountSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val amounts = listOf(250, 500, 750)
    val neonCyan = Color(0xFF06F9F9)

    // Basit Box ile arkaplan + Row ile butonlar
    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.large
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            amounts.forEach { amount ->
                val isSelected = selectedAmount == amount

                // Basit Box buton olarak kullan
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = if (isSelected) neonCyan else Color.Transparent,
                            shape = MaterialTheme.shapes.medium
                        )
                        .shadow(
                            elevation = if (isSelected) 4.dp else 0.dp,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { onAmountSelected(amount) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${amount}ml",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Add Water Button - Güncellendi (Glow efektiyle)
 */
@Composable
fun AddWaterButton(
    amount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val neonCyan = Color(0xFF06F9F9)

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 12.dp,
                shape = MaterialTheme.shapes.large,
                spotColor = neonCyan.copy(alpha = 0.4f)
            ),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = neonCyan
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.Black
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.add_water),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

/**
 * Daily Stats Section - Güncellendi (HTML'deki gibi)
 */
@Composable
fun DailyStatsSection(
    modifier: Modifier = Modifier
) {
    val timeSlots = listOf("6-9", "9-12", "12-15", "15-18", "18-21")
    val percentages = listOf(0.4f, 0.1f, 0.8f, 0.3f, 0.0f)
    val neonCyan = Color(0xFF06F9F9)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.daily_stats),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Bar Chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    timeSlots.forEachIndexed { index, timeSlot ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Bar
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .fillMaxHeight(percentages[index])
                                    .clip(MaterialTheme.shapes.small)
                                    .shadow(
                                        elevation = if (percentages[index] > 0) 4.dp else 0.dp,
                                        shape = MaterialTheme.shapes.small,
                                        spotColor = if (percentages[index] > 0) neonCyan.copy(alpha = 0.3f) else Color.Transparent
                                    )
                                    .background(
                                        color = if (percentages[index] > 0) neonCyan
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                            )

                            // Time label
                            Text(
                                text = timeSlot,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Recent Activities Section - Güncellendi
 */
@Composable
fun RecentActivitiesSection(
    modifier: Modifier = Modifier
) {
    val activities = listOf(
        Triple(500, "14:32", "Harika gidiyorsun!"),
        Triple(250, "12:15", ""),
        Triple(750, "09:04", "")
    )
    val neonCyan = Color(0xFF06F9F9)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.recent_activities),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            activities.forEach { (amount, time, message) ->
                WaterActivityCard(
                    amount = amount,
                    time = time,
                    message = message,
                    neonColor = neonCyan
                )
            }
        }
    }
}

/**
 * Water Activity Card - Güncellendi
 */
@Composable
fun WaterActivityCard(
    amount: Int,
    time: String,
    message: String,
    neonColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(neonColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_water_drop),
                        contentDescription = stringResource(R.string.cd_water_drop),
                        tint = neonColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "$amount ml",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================
// PREVIEW
// ============================================
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun WaterTrackerScreenPreview() {
    ModaiTheme(darkTheme = false) {
        WaterTrackerScreen()
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun WaterTrackerScreenDarkPreview() {
    ModaiTheme(darkTheme = true) {
        WaterTrackerScreen()
    }
}