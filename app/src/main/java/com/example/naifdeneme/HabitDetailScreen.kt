@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.naifdeneme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naifdeneme.database.AppDatabase
import com.example.naifdeneme.database.HabitEntity
import kotlinx.coroutines.launch

@Composable
fun HabitDetailScreen(
    habitId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var habit by remember { mutableStateOf<HabitEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showDaysDialog by remember { mutableStateOf(false) }

    LaunchedEffect(habitId) {
        habit = database.habitDao().getHabitById(habitId)
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentHabit = habit
    if (currentHabit == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Habit bulunamadı")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(currentHabit.icon, fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(currentHabit.name)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Edit */ }) {
                        Icon(Icons.Default.Edit, "Düzenle")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StreakCard(habit = currentHabit)
            StatisticsCard(habit = currentHabit)
            CalendarCard(habit = currentHabit)
            ReminderCard(
                habit = currentHabit,
                onReminderToggle = { enabled ->
                    scope.launch {
                        val updated = currentHabit.copy(reminderEnabled = enabled)
                        database.habitDao().updateHabit(updated)
                        habit = updated

                        if (enabled) {
                            NotificationHelper.scheduleHabitReminder(
                                context,
                                updated.id,
                                updated.reminderHour,
                                updated.reminderMinute,
                                updated.reminderDays
                            )
                        } else {
                            NotificationHelper.cancelHabitReminder(context, updated.id)
                        }
                    }
                },
                onTimeClick = { showReminderDialog = true },
                onDaysClick = { showDaysDialog = true }
            )
            ActionButtons(
                habit = currentHabit,
                onComplete = {
                    scope.launch {
                        database.habitDao().completeHabit(habitId)
                        habit = database.habitDao().getHabitById(habitId)
                    }
                },
                onUncomplete = {
                    scope.launch {
                        database.habitDao().uncompleteHabit(habitId)
                        habit = database.habitDao().getHabitById(habitId)
                    }
                },
                onSkip = {
                    scope.launch {
                        database.habitDao().skipHabit(habitId)
                        habit = database.habitDao().getHabitById(habitId)
                    }
                }
            )
        }
    }

    if (showReminderDialog) {
        TimePickerDialog(
            initialHour = currentHabit.reminderHour,
            initialMinute = currentHabit.reminderMinute,
            onDismiss = { showReminderDialog = false },
            onConfirm = { hour, minute ->
                scope.launch {
                    val updated = currentHabit.copy(
                        reminderHour = hour,
                        reminderMinute = minute
                    )
                    database.habitDao().updateHabit(updated)
                    habit = updated

                    if (updated.reminderEnabled) {
                        NotificationHelper.scheduleHabitReminder(
                            context,
                            updated.id,
                            hour,
                            minute,
                            updated.reminderDays
                        )
                    }
                }
                showReminderDialog = false
            }
        )
    }

    if (showDaysDialog) {
        DaysPickerDialog(
            selectedDays = currentHabit.reminderDays,
            onDismiss = { showDaysDialog = false },
            onConfirm = { days ->
                scope.launch {
                    val updated = currentHabit.copy(reminderDays = days)
                    database.habitDao().updateHabit(updated)
                    habit = updated

                    if (updated.reminderEnabled) {
                        NotificationHelper.scheduleHabitReminder(
                            context,
                            updated.id,
                            updated.reminderHour,
                            updated.reminderMinute,
                            days
                        )
                    }
                }
                showDaysDialog = false
            }
        )
    }
}

@Composable
fun StreakCard(habit: HabitEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🔥 Mevcut Streak",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "${habit.currentStreak} Gün",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            if (habit.longestStreak > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "🏆 En Uzun: ${habit.longestStreak} Gün",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "💪 Güç:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                LinearProgressIndicator(
                    progress = habit.strengthScore,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${(habit.strengthScore * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun StatisticsCard(habit: HabitEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "📊 İstatistikler",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Tamamlama Oranı",
                    value = "${habit.getCompletionRate()}%",
                    icon = "✅"
                )
                StatItem(
                    label = "Toplam Tamamlama",
                    value = "${habit.totalCompletions}",
                    icon = "🎯"
                )
                StatItem(
                    label = "En Uzun Streak",
                    value = "${habit.longestStreak}",
                    icon = "🏆"
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 24.sp)
        Text(
            value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CalendarCard(habit: HabitEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "📅 Son 30 Gün",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            val last30Days = habit.getLast30DaysCompletion()

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(last30Days) { isCompleted ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.width(4.dp))
                Text("Tamamlandı", fontSize = 12.sp)
                Spacer(Modifier.width(16.dp))
                Box(
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.width(4.dp))
                Text("Eksik", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ReminderCard(
    habit: HabitEntity,
    onReminderToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
    onDaysClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
                    "🔔 Hatırlatma",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = habit.reminderEnabled,
                    onCheckedChange = onReminderToggle
                )
            }

            if (habit.reminderEnabled) {
                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onTimeClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, null)
                    Spacer(Modifier.width(8.dp))
                    Text("${String.format("%02d:%02d", habit.reminderHour, habit.reminderMinute)}")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDaysClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Info, null)
                    Spacer(Modifier.width(8.dp))
                    Text(getDaysText(habit.reminderDays))
                }
            }
        }
    }
}

@Composable
fun ActionButtons(
    habit: HabitEntity,
    onComplete: () -> Unit,
    onUncomplete: () -> Unit,
    onSkip: () -> Unit
) {
    val isCompletedToday = habit.isCompletedToday()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isCompletedToday) {
            Button(
                onClick = onUncomplete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Clear, null)
                Spacer(Modifier.width(8.dp))
                Text("Tamamlamayı Geri Al")
            }
        } else {
            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("Bugün Tamamla")
            }

            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Close, null)
                Spacer(Modifier.width(8.dp))
                Text("Bugün Skip (Streak Korunur)")
            }
        }
    }
}

@Composable
fun DaysPickerDialog(
    selectedDays: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val days = remember {
        mutableStateListOf<Int>().apply {
            addAll(selectedDays.split(",").mapNotNull { it.toIntOrNull() })
        }
    }

    val dayNames = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hatırlatma Günleri") },
        text = {
            Column {
                dayNames.forEachIndexed { index, name ->
                    val dayValue = index + 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name)
                        Checkbox(
                            checked = days.contains(dayValue),
                            onCheckedChange = { checked ->
                                if (checked) {
                                    days.add(dayValue)
                                } else {
                                    days.remove(dayValue)
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val result = days.sorted().joinToString(",")
                    onConfirm(result)
                }
            ) {
                Text("Tamam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

fun getDaysText(days: String): String {
    val daysList = days.split(",").mapNotNull { it.toIntOrNull() }
    if (daysList.size == 7) return "Her gün"
    if (daysList.isEmpty()) return "Gün seçilmedi"

    val dayNames = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
    return daysList.map { dayNames.getOrNull(it - 1) ?: "" }.joinToString(", ")
}

@Composable
    fun TimePickerDialog(
        initialHour: Int,
        initialMinute: Int,
        onDismiss: () -> Unit,
        onConfirm: (Int, Int) -> Unit
    ) {
        var selectedHour by remember { mutableStateOf(initialHour) }
        var selectedMinute by remember { mutableStateOf(initialMinute) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Hatırlatma Saati") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⏰", fontSize = 24.sp)
                        Text(
                            String.format("%02d:%02d", selectedHour, selectedMinute),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text("Saat", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { if (selectedHour > 0) selectedHour-- else selectedHour = 23 }) {
                                Icon(Icons.Default.KeyboardArrowDown, null)
                            }
                            Text(String.format("%02d", selectedHour), fontSize = 24.sp, modifier = Modifier.width(48.dp))
                            IconButton(onClick = { if (selectedHour < 23) selectedHour++ else selectedHour = 0 }) {
                                Icon(Icons.Default.KeyboardArrowUp, null)
                            }
                        }
                    }

                    Column {
                        Text("Dakika", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { if (selectedMinute > 0) selectedMinute-- else selectedMinute = 59 }) {
                                Icon(Icons.Default.KeyboardArrowDown, null)
                            }
                            Text(String.format("%02d", selectedMinute), fontSize = 24.sp, modifier = Modifier.width(48.dp))
                            IconButton(onClick = { if (selectedMinute < 59) selectedMinute++ else selectedMinute = 0 }) {
                                Icon(Icons.Default.KeyboardArrowUp, null)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(selectedHour, selectedMinute) }) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("İptal")
                }
            }
        )
    }
