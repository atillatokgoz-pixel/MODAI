@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.naifdeneme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naifdeneme.database.AppDatabase
import com.example.naifdeneme.database.HabitEntity
import kotlinx.coroutines.launch

@Composable
fun HabitScreen(onNavigateToDetail: (Long) -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val habits by database.habitDao().getAllHabits().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<HabitEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Alışkanlıklar") },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Alışkanlık Ekle")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💪", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Henüz alışkanlık yok", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Alışkanlık Ekle")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    HabitCard(
                        habit = habit,
                        onComplete = {
                            scope.launch {
                                if (habit.isCompletedToday()) {
                                    // 🔁 Artık DAO fonksiyonu ile geri alıyoruz
                                    database.habitDao().uncompleteHabit(habit.id)
                                } else {
                                    database.habitDao().completeHabit(habit.id)
                                }
                            }
                        },
                        onDelete = { habitToDelete = habit },
                        onClick = { onNavigateToDetail(habit.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name ->
                scope.launch {
                    database.habitDao().insertHabit(
                        HabitEntity(
                            name = name,
                            icon = "💪",
                            color = 0xFFFF6B6B
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }

    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Alışkanlığı Sil") },
            text = { Text("'${habit.name}' alışkanlığını silmek istediğinizden emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { database.habitDao().deleteHabit(habit) }
                        habitToDelete = null
                    }
                ) { Text("Sil", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) { Text("İptal") }
            }
        )
    }
}

@Composable
fun HabitCard(
    habit: HabitEntity,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val isCompletedToday = habit.isCompletedToday()
    val completionRate = habit.getCompletionRate().coerceIn(0, 100)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

            // SOL TARAF: İKON + İSİM + BUGÜNKÜ DURUM
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(habit.icon, fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        habit.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (habit.currentStreak > 0) {
                            Text(
                                "🔥 ${habit.currentStreak}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (habit.reminderEnabled) {
                            Text(
                                "🔔 %02d:%02d".format(habit.reminderHour, habit.reminderMinute),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Text(
                            if (isCompletedToday) "✅ Bugün tamamlandı" else "⏳ Tamamlanmadı",
                            fontSize = 12.sp,
                            color = if (isCompletedToday)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // SAĞ TARAF: TAMAMLAMA ORANI + BUTONLAR
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tamamlama oranı
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (completionRate >= 100) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Tamamlandı",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            "$completionRate%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onComplete) {
                        Icon(
                            imageVector = if (isCompletedToday)
                                Icons.Default.CheckCircle
                            else
                                Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isCompletedToday)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Add, null) },
        title = { Text("Yeni Alışkanlık") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Alışkanlık adı") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onAdd(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HabitScreenPreview() {
    com.example.naifdeneme.ui.theme.ModaiTheme {
        HabitScreen(onNavigateToDetail = {})
    }
}
