package com.example.naifdeneme

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterReminderSettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewModel: WaterReminderSettingsViewModel = viewModel(
        factory = WaterReminderSettingsViewModelFactory(
            preferencesManager = PreferencesManager.getInstance(context)
        )
    )

    // State Collection
    val reminderEnabled by viewModel.reminderEnabled.collectAsState()
    val frequency by viewModel.frequency.collectAsState()
    val startHour by viewModel.startHour.collectAsState()
    val endHour by viewModel.endHour.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.water_reminder_settings),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 1. ENABLE/DISABLE CARD
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.enable_reminders),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.reminder_enable_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { isChecked ->
                            viewModel.setReminderEnabled(isChecked)

                            scope.launch {
                                if (isChecked) {
                                    WaterReminderScheduler.scheduleReminders(context)
                                    Toast.makeText(context, context.getString(R.string.reminders_enabled), Toast.LENGTH_SHORT).show()
                                } else {
                                    WaterReminderScheduler.cancelReminders(context)
                                    Toast.makeText(context, context.getString(R.string.reminders_disabled), Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            // ==========================================
            // 2. FREQUENCY CARD
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(
                        alpha = if (reminderEnabled) 1f else 0.6f // Görsel olarak disable et
                    )
                )
                // enabled parametresini SİLDİK
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.reminder_frequency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = stringResource(R.string.every_x_minutes, frequency),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.frequency_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(16.dp))

                    Slider(
                        value = frequency.toFloat(),
                        onValueChange = { viewModel.setFrequency(it.toInt()) },
                        valueRange = 15f..180f,
                        steps = 10,
                        enabled = reminderEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("15 dk", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("3 saat", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ==========================================
            // 3. TIME RANGE CARD
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.active_hours),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.active_hours_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedCard(
                            onClick = { if (reminderEnabled) showStartTimePicker = true },
                            modifier = Modifier.weight(1f),
                            enabled = reminderEnabled
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(8.dp))
                                Text(stringResource(R.string.start_time), style = MaterialTheme.typography.labelSmall)
                                Text(String.format("%02d:00", startHour), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedCard(
                            onClick = { if (reminderEnabled) showEndTimePicker = true },
                            modifier = Modifier.weight(1f),
                            enabled = reminderEnabled
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(8.dp))
                                Text(stringResource(R.string.end_time), style = MaterialTheme.typography.labelSmall)
                                Text(String.format("%02d:00", endHour), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. NOTIFICATION PREFERENCES
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.notification_preferences),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Sound Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(stringResource(R.string.notification_sound), style = MaterialTheme.typography.bodyLarge)
                                Text(stringResource(R.string.notification_sound_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { viewModel.setSoundEnabled(it) },
                            enabled = reminderEnabled
                        )
                    }

                    Divider()

                    // Vibration Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(stringResource(R.string.vibration), style = MaterialTheme.typography.bodyLarge)
                                Text(stringResource(R.string.vibration_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { viewModel.setVibrationEnabled(it) },
                            enabled = reminderEnabled
                        )
                    }
                }
            }

            // ==========================================
            // 5. TEST BUTTON
            // ==========================================
            Button(
                onClick = {
                    WaterReminderScheduler.scheduleTestReminder(context)
                    Toast.makeText(context, context.getString(R.string.test_notification_scheduled), Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = reminderEnabled
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.send_test_notification))
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(
                        text = stringResource(R.string.reminder_info_text),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }

    // Time Picker Dialogs
    if (showStartTimePicker) {
        TimePickerDialog(context, { _, hour, _ ->
            if (hour < endHour) {
                viewModel.setStartHour(hour)
                scope.launch { WaterReminderScheduler.scheduleReminders(context) }
            } else {
                Toast.makeText(context, context.getString(R.string.start_time_must_be_before_end), Toast.LENGTH_SHORT).show()
            }
            showStartTimePicker = false
        }, startHour, 0, true).show()
    }

    if (showEndTimePicker) {
        TimePickerDialog(context, { _, hour, _ ->
            if (hour > startHour) {
                viewModel.setEndHour(hour)
                scope.launch { WaterReminderScheduler.scheduleReminders(context) }
            } else {
                Toast.makeText(context, context.getString(R.string.end_time_must_be_after_start), Toast.LENGTH_SHORT).show()
            }
            showEndTimePicker = false
        }, endHour, 0, true).show()
    }
}

// ==========================================
// VIEWMODEL
// ==========================================
class WaterReminderSettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val reminderEnabled: StateFlow<Boolean> = preferencesManager.waterReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val frequency: StateFlow<Int> = preferencesManager.waterReminderFrequency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60)

    val startHour: StateFlow<Int> = preferencesManager.waterReminderStartHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8)

    val endHour: StateFlow<Int> = preferencesManager.waterReminderEndHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 22)

    val soundEnabled: StateFlow<Boolean> = preferencesManager.waterNotificationSound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val vibrationEnabled: StateFlow<Boolean> = preferencesManager.waterNotificationVibration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setWaterReminderEnabled(enabled) }
    }

    fun setFrequency(minutes: Int) {
        viewModelScope.launch {
            val validFrequency = minutes.coerceIn(15, 180)
            preferencesManager.setWaterReminderFrequency(validFrequency)
        }
    }

    fun setStartHour(hour: Int) {
        viewModelScope.launch { preferencesManager.setWaterReminderStartHour(hour.coerceIn(0, 23)) }
    }

    fun setEndHour(hour: Int) {
        viewModelScope.launch { preferencesManager.setWaterReminderEndHour(hour.coerceIn(0, 23)) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setWaterNotificationSound(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setWaterNotificationVibration(enabled) }
    }
}

class WaterReminderSettingsViewModelFactory(
    private val preferencesManager: PreferencesManager
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterReminderSettingsViewModel::class.java)) {
            return WaterReminderSettingsViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}