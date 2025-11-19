package com.example.naifdeneme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.naifdeneme.database.AppDatabase
import kotlinx.coroutines.launch

/**
 * Su Hatırlatıcı Ayarları Ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsManager = remember { PreferencesManager.getInstance(context) }

    // ViewModel
    val viewModel: WaterViewModel = viewModel(
        factory = WaterViewModelFactory(
            waterDao = AppDatabase.getDatabase(context).waterDao(),
            preferencesManager = prefsManager
        )
    )

    // States
    val reminderEnabled by prefsManager.waterReminderEnabled.collectAsState(initial = false)
    val startHour by prefsManager.waterReminderStartHour.collectAsState(initial = 9)
    val endHour by prefsManager.waterReminderEndHour.collectAsState(initial = 22)
    val frequency by prefsManager.waterReminderFrequency.collectAsState(initial = 60)

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    val neonCyan = Color(0xFF06F9F9)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reminder_settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                // Bildirimleri planla
                                if (reminderEnabled) {
                                    WaterReminderScheduler.scheduleReminders(context)
                                } else {
                                    WaterReminderScheduler.cancelReminders(context)
                                }
                                hasUnsavedChanges = false
                            }
                            onNavigateBack()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.reminder_save),
                            color = neonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Master Switch
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                                .background(neonCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = neonCyan
                            )
                        }

                        Text(
                            text = stringResource(R.string.reminder_enable_reminders),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                prefsManager.setWaterReminderEnabled(enabled)
                                hasUnsavedChanges = true
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = neonCyan
                        )
                    )
                }
            }

            // Zamanlama Section
            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.reminder_timing_section),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Sıklık
                    ReminderSettingItem(
                        icon = Icons.Default.Refresh,
                        title = stringResource(R.string.reminder_frequency),
                        value = getFrequencyText(frequency),
                        onClick = { if (reminderEnabled) showFrequencyDialog = true },
                        enabled = reminderEnabled
                    )

                    Divider(modifier = Modifier.padding(start = 68.dp))

                    // Başlangıç saati
                    ReminderSettingItem(
                        icon = Icons.Default.DateRange,
                        title = stringResource(R.string.reminder_start_time),
                        value = String.format("%02d:00", startHour),
                        onClick = { if (reminderEnabled) showStartTimePicker = true },
                        enabled = reminderEnabled
                    )

                    Divider(modifier = Modifier.padding(start = 68.dp))

                    // Bitiş saati
                    ReminderSettingItem(
                        icon = Icons.Default.DateRange,
                        title = stringResource(R.string.reminder_end_time),
                        value = String.format("%02d:00", endHour),
                        onClick = { if (reminderEnabled) showEndTimePicker = true },
                        enabled = reminderEnabled
                    )
                }
            }

            // Info Card
            if (reminderEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = stringResource(
                                R.string.reminder_info,
                                startHour,
                                endHour,
                                frequency / 60
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }

    // Time Pickers
    if (showStartTimePicker) {
        TimePickerDialog(
            title = stringResource(R.string.reminder_start_time),
            currentHour = startHour,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { hour ->
                scope.launch {
                    prefsManager.setWaterReminderStartHour(hour)
                    hasUnsavedChanges = true
                }
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            title = stringResource(R.string.reminder_end_time),
            currentHour = endHour,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { hour ->
                scope.launch {
                    prefsManager.setWaterReminderEndHour(hour)
                    hasUnsavedChanges = true
                }
                showEndTimePicker = false
            }
        )
    }

    if (showFrequencyDialog) {
        FrequencyDialog(
            currentFrequency = frequency,
            onDismiss = { showFrequencyDialog = false },
            onConfirm = { newFrequency ->
                scope.launch {
                    prefsManager.setWaterReminderFrequency(newFrequency)
                    hasUnsavedChanges = true
                }
                showFrequencyDialog = false
            }
        )
    }
}

@Composable
fun ReminderSettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val neonCyan = Color(0xFF06F9F9)

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) neonCyan.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) neonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.width(8.dp))

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TimePickerDialog(
    title: String,
    currentHour: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(currentHour) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.DateRange, null) },
        title = { Text(title) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%02d:00", selectedHour),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                Slider(
                    value = selectedHour.toFloat(),
                    onValueChange = { selectedHour = it.toInt() },
                    valueRange = 0f..23f,
                    steps = 22,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF06F9F9),
                        activeTrackColor = Color(0xFF06F9F9)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("00:00", style = MaterialTheme.typography.bodySmall)
                    Text("23:00", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedHour) }) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@Composable
fun FrequencyDialog(
    currentFrequency: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val frequencies = listOf(
        30 to "Her 30 dakika",
        60 to "Her saat",
        120 to "Her 2 saat",
        180 to "Her 3 saat"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Refresh, null) },
        title = { Text(stringResource(R.string.reminder_frequency)) },
        text = {
            Column {
                frequencies.forEach { (minutes, label) ->
                    val isSelected = currentFrequency == minutes

                    Surface(
                        onClick = {
                            onConfirm(minutes)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            Color.Transparent,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (frequencies.last().first != minutes) {
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

fun getFrequencyText(minutes: Int): String {
    return when (minutes) {
        30 -> "Her 30 dk"
        60 -> "Her saat"
        120 -> "Her 2 saat"
        180 -> "Her 3 saat"
        else -> "Her $minutes dk"
    }
}