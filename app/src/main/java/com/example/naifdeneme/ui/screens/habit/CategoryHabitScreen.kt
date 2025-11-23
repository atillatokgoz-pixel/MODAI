package com.example.naifdeneme.ui.screens.habit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naifdeneme.HabitCard
import com.example.naifdeneme.database.AppDatabase
import com.example.naifdeneme.database.HabitEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryHabitScreen(
    category: String, // Örn: "HEALTH", "WORK"
    onBack: () -> Unit,
    onAddHabit: (String) -> Unit // Kategori bilgisini göndererek ekleme ekranına git
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    // Kategoriye göre alışkanlıkları çek
    val habits by database.habitDao().getHabitsByCategory(category).collectAsState(initial = emptyList())

    // Kategori başlığı ve rengini belirle
    val (categoryTitle, categoryColor) = when (category) {
        "HEALTH" -> "Sağlık" to Color(0xFFFFC2D1)
        "WORK" -> "İş" to Color(0xFFF0E68C)
        "FINANCE" -> "Finans" to Color(0xFFA2E4B8)
        "EDUCATION" -> "Eğitim" to Color(0xFFA3D5FF)
        else -> "Diğer" to Color.Gray
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(categoryTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = categoryColor.copy(alpha = 0.3f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddHabit(category) }, // Bu kategoride ekle
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Ekle")
            }
        }
    ) { padding ->
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Henüz alışkanlık yok",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "+ butonuna basarak ekle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(habits) { habit ->
                    // Mevcut HabitCard bileşenini kullanıyoruz
                    HabitCard(
                        habit = habit,
                        onComplete = {
                            scope.launch {
                                if (habit.isCompletedToday()) {
                                    database.habitDao().uncompleteHabit(habit.id)
                                } else {
                                    database.habitDao().completeHabit(habit.id)
                                }
                            }
                        },
                        onDelete = {
                            scope.launch { database.habitDao().deleteHabit(habit) }
                        },
                        onClick = { /* Detay sayfasına git */ }
                    )
                }
            }
        }
    }
}