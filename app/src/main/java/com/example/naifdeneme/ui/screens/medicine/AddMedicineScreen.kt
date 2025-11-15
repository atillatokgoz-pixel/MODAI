package com.example.naifdeneme.ui.screens.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.naifdeneme.R
import com.example.naifdeneme.ui.theme.ModaiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    onCancel: () -> Unit,
    onSave: (MedicineData) -> Unit
) {
    var medicineName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf(Frequency.DAILY) }
    var reminderTimes by remember { mutableStateOf(listOf("08:00")) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var hasEndDate by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }

    var showFrequencyDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Text(
                        text = stringResource(R.string.medicine_add),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text(
                            text = stringResource(R.string.cancel),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (medicineName.isNotBlank() && reminderTimes.isNotEmpty()) {
                                onSave(
                                    MedicineData(
                                        name = medicineName,
                                        dosage = dosage,
                                        frequency = selectedFrequency,
                                        reminderTimes = reminderTimes,
                                        startDate = startDate,
                                        endDate = if (hasEndDate) endDate else null,
                                        soundEnabled = soundEnabled,
                                        vibrationEnabled = vibrationEnabled
                                    )
                                )
                            }
                        },
                        enabled = medicineName.isNotBlank() && reminderTimes.isNotEmpty()
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Information Section
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = medicineName,
                            onValueChange = { medicineName = it },
                            label = { Text(stringResource(R.string.medicine_name_hint)) },
                            placeholder = { Text("örn. D Vitamini") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = dosage,
                            onValueChange = { dosage = it },
                            label = { Text(stringResource(R.string.medicine_dosage)) },
                            placeholder = { Text("örn. 1 Tablet") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Frequency and Time Section
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Frequency Dropdown
                        Box {
                            TimeSettingRow(
                                icon = Icons.Default.CalendarToday,
                                title = stringResource(R.string.medicine_frequency),
                                value = selectedFrequency.displayName,
                                onClick = { showFrequencyDropdown = true }
                            )

                            DropdownMenu(
                                expanded = showFrequencyDropdown,
                                onDismissRequest = { showFrequencyDropdown = false }
                            ) {
                                Frequency.entries.forEach { frequency ->
                                    DropdownMenuItem(
                                        text = { Text(frequency.displayName) },
                                        onClick = {
                                            selectedFrequency = frequency
                                            showFrequencyDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Time Settings
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.medicine_time),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                IconButton(
                                    onClick = {
                                        reminderTimes = reminderTimes + "08:00"
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(R.string.add_time),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            reminderTimes.forEachIndexed { index, time ->
                                TimeInputRow(
                                    time = time,
                                    onTimeChange = { newTime ->
                                        reminderTimes = reminderTimes.toMutableList().apply {
                                            set(index, newTime)
                                        }
                                    },
                                    onDelete = {
                                        reminderTimes = reminderTimes.toMutableList().apply {
                                            removeAt(index)
                                        }
                                    },
                                    showDelete = reminderTimes.size > 1
                                )
                            }
                        }
                    }
                }
            }

            // Date Range Section
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Tedavi Süresi",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Start Date
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.medicine_start_date),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = startDate.ifEmpty { "Bugün" },
                                    onValueChange = { startDate = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Event,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }

                            // End Date
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.medicine_end_date),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = if (hasEndDate) endDate else "Yok",
                                    onValueChange = { if (hasEndDate) endDate = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.EventAvailable,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = hasEndDate,
                                onCheckedChange = { hasEndDate = it }
                            )
                            Text("Bitiş tarihi belirle")
                        }
                    }
                }
            }

            // Notification Settings
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NotificationSettingRow(
                            title = "Ses",
                            isEnabled = soundEnabled,
                            onToggle = { soundEnabled = it }
                        )

                        NotificationSettingRow(
                            title = "Titreşim",
                            isEnabled = vibrationEnabled,
                            onToggle = { vibrationEnabled = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TimeInputRow(
    time: String,
    onTimeChange: (String) -> Unit,
    onDelete: () -> Unit,
    showDelete: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = time,
            onValueChange = onTimeChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("HH:MM") }
        )

        if (showDelete) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = "Sil")
            }
        }
    }
}

@Composable
private fun NotificationSettingRow(
    title: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle
        )
    }
}

// Data Models
data class MedicineData(
    val name: String,
    val dosage: String,
    val frequency: Frequency,
    val reminderTimes: List<String>,
    val startDate: String,
    val endDate: String?,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean
)

enum class Frequency(val displayName: String) {
    DAILY("Her Gün"),
    SPECIFIC_DAYS("Belirli Günler"),
    AS_NEEDED("Gerektiğinde")
}

@Preview(showBackground = true)
@Composable
fun AddMedicineScreenPreview() {
    ModaiTheme {
        AddMedicineScreen(
            onCancel = {},
            onSave = {}
        )
    }
}