package com.example.naifdeneme.ui.screens.water

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.naifdeneme.PreferencesManager
import com.example.naifdeneme.R
import com.example.naifdeneme.WaterViewModel
import com.example.naifdeneme.WaterViewModelFactory
import com.example.naifdeneme.database.AppDatabase
import com.example.naifdeneme.ui.screens.water.components.AnimatedWaterProgressRing
import com.example.naifdeneme.ui.theme.ModaiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToReminderSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: WaterViewModel = viewModel(
        factory = WaterViewModelFactory(
            waterDao = AppDatabase.getDatabase(context).waterDao(),
            preferencesManager = PreferencesManager.getInstance(context)
        )
    )

    val currentAmount by viewModel.todayTotal.collectAsState()
    val targetAmount by viewModel.dailyTarget.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isGoalReached by viewModel.isGoalReached.collectAsState()
    val reminderEnabled by viewModel.reminderEnabled.collectAsState()

    var selectedAmount by remember { mutableStateOf(500) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    var buttonScale by remember { mutableStateOf(1f) }

    LaunchedEffect(isGoalReached) {
        if (isGoalReached && !showCelebration) {
            showCelebration = true
            delay(3000)
            showCelebration = false
        }
    }

    val neonCyan = Color(0xFF06F9F9)

    Scaffold(
        topBar = {
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
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                            contentDescription = stringResource(R.string.water_history),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.clickable { showTargetDialog = true }
                    ) {
                        AnimatedWaterProgressRing(
                            currentAmount = currentAmount,
                            targetAmount = targetAmount,
                            animate = true
                        )
                    }

                    if (isGoalReached) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "🎉 " + stringResource(R.string.water_goal_reached),
                            style = MaterialTheme.typography.titleMedium,
                            color = neonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    onClick = onNavigateToReminderSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
                                    text = if (reminderEnabled)
                                        stringResource(R.string.reminder_on)
                                    else
                                        stringResource(R.string.reminder_off),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateReminderEnabled(enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = neonCyan,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
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
                        listOf(250, 500, 750).forEach { amount ->
                            val isSelected = selectedAmount == amount
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
                                    .clickable { selectedAmount = amount },
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

                Spacer(modifier = Modifier.height(24.dp))

                val animatedScale by animateFloatAsState(
                    targetValue = buttonScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    finishedListener = { buttonScale = 1f },
                    label = "button_scale"
                )

                Button(
                    onClick = {
                        viewModel.addWater(selectedAmount)
                        buttonScale = 0.95f
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp)
                        .scale(animatedScale)
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
                        text = stringResource(R.string.add_water, selectedAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
                            val weeklyStats by viewModel.weeklyStats.collectAsState()
                            val last5Days = weeklyStats.takeLast(5)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                last5Days.forEach { stat ->
                                    val percentage = if (targetAmount > 0) {
                                        (stat.amount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f)
                                    } else 0f

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .fillMaxHeight(percentage)
                                                .clip(MaterialTheme.shapes.small)
                                                .shadow(
                                                    elevation = if (percentage > 0) 4.dp else 0.dp,
                                                    shape = MaterialTheme.shapes.small,
                                                    spotColor = if (percentage > 0) neonCyan.copy(alpha = 0.3f) else Color.Transparent
                                                )
                                                .background(
                                                    color = if (percentage > 0) neonCyan
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                )
                                        )

                                        Text(
                                            text = stat.dayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.recent_activities),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    val entries by viewModel.todayEntries.collectAsState()
                    val recentEntries = entries.take(3)

                    if (recentEntries.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.no_water_entries_today),
                                modifier = Modifier.padding(24.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentEntries.forEach { entry ->
                                val time = remember(entry.timestamp) {
                                    val calendar = java.util.Calendar.getInstance()
                                    calendar.timeInMillis = entry.timestamp
                                    String.format("%02d:%02d", calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE))
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
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
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(neonCyan.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_water_drop),
                                                    contentDescription = stringResource(R.string.cd_water_drop),
                                                    tint = neonCyan,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = "${entry.amount} ml",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (!entry.note.isNullOrEmpty()) {
                                                    Text(
                                                        text = entry.note,
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
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showCelebration) {
                val infiniteTransition = rememberInfiniteTransition(label = "celebration")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎉 🎊 ✨\nTebrikler!\nGünlük Hedef Tamamlandı!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF06F9F9),
                        modifier = Modifier.scale(scale),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    if (showTargetDialog) {
        var targetValue by remember { mutableStateOf(targetAmount) }
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_water_drop),
                    contentDescription = null,
                    tint = Color(0xFF06F9F9)
                )
            },
            title = { Text(stringResource(R.string.water_daily_target)) },
            text = {
                Column {
                    Text(stringResource(R.string.set_daily_target_desc))
                    Spacer(Modifier.height(16.dp))

                    Slider(
                        value = targetValue.toFloat(),
                        onValueChange = { targetValue = it.toInt() },
                        valueRange = 1000f..5000f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF06F9F9),
                            activeTrackColor = Color(0xFF06F9F9)
                        )
                    )

                    Text(
                        text = "$targetValue ml",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateDailyTarget(targetValue)
                    showTargetDialog = false
                }) {
                    Text(stringResource(R.string.dialog_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WaterTrackerScreenPreview() {
    ModaiTheme(darkTheme = false) {
        WaterTrackerScreen(
            onNavigateBack = {},
            onNavigateToSettings = {},
            onNavigateToHistory = {},
            onNavigateToReminderSettings = {}
        )
    }
}