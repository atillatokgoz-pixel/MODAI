package com.example.naifdeneme.ui.screens.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naifdeneme.HabitCard
import com.example.naifdeneme.R
import com.example.naifdeneme.database.AppDatabase
import com.example.naifdeneme.model.HabitCategory
import com.example.naifdeneme.model.displayName
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.naifdeneme.database.TransactionType


/**
 * CategoryHubScreen - Akıllı Kategori Sayfası
 *
 * Her kategori için özel "Hub" görünümü sunar.
 * Örn: HEALTH -> Su Takibi Kartı + İlaç Kartı + Alışkanlık Listesi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryHubScreen(
    categoryName: String, // "HEALTH", "WORK" vb.
    onBack: () -> Unit,
    onNavigateToModule: (String) -> Unit, // "water", "pomodoro" vb.
    onAddHabit: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }

    // String'den Enum'a çevir (Hata yönetimi ile)
    val category = try {
        HabitCategory.valueOf(categoryName)
    } catch (e: Exception) {
        HabitCategory.PERSONAL
    }

    // Alışkanlıkları çek
    val habits by database.habitDao().getHabitsByCategory(categoryName).collectAsState(initial = emptyList())

    // Kategori Tema Rengi
    val themeColor = when(category) {
        HabitCategory.HEALTH -> Color(0xFFFFC2D1) // Pink
        HabitCategory.WORK -> Color(0xFFF0E68C) // Yellow
        HabitCategory.FINANCE -> Color(0xFFA2E4B8) // Green
        HabitCategory.EDUCATION -> Color(0xFFA3D5FF) // Blue
        HabitCategory.FITNESS -> Color(0xFFFFD180) // Orange
        HabitCategory.PERSONAL -> Color(0xFFE1BEE7) // Purple
    }

    val darkThemeColor = when(category) {
        HabitCategory.HEALTH -> Color(0xFFD81B60)
        HabitCategory.WORK -> Color(0xFFFBC02D)
        HabitCategory.FINANCE -> Color(0xFF388E3C)
        HabitCategory.EDUCATION -> Color(0xFF1976D2)
        HabitCategory.FITNESS -> Color(0xFFF57C00)
        HabitCategory.PERSONAL -> Color(0xFF7B1FA2)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        category.displayName(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = themeColor.copy(alpha = 0.3f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddHabit(categoryName) },
                containerColor = darkThemeColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Ekle")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 1. ÜST MODÜL KARTLARI (Kategoriye Özel)
            item {
                when(category) {
                    HabitCategory.HEALTH -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            WaterSummaryCard(database, onClick = { onNavigateToModule("water") })
                            // MedicineSummaryCard(database, onClick = { onNavigateToModule("medicine") }) // İleride eklenecek
                        }
                    }
                    HabitCategory.WORK -> {
                        PomodoroSummaryCard(database, onClick = { onNavigateToModule("pomodoro") })
                    }
                    HabitCategory.FINANCE -> {
                        FinanceSummaryCard(database, onClick = { onNavigateToModule("finance") })
                    }
                    HabitCategory.EDUCATION -> {
                        NotesSummaryCard(database, onClick = { onNavigateToModule("notes") })
                    }
                    else -> {
                        // Fitness veya Personal için özel kart yok (şimdilik)
                    }
                }
            }

            // 2. AYIRAÇ VE BAŞLIK
            item {
                if (habits.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Alışkanlıklar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3. ALIŞKANLIK LİSTESİ
            if (habits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✨", fontSize = 48.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Bu kategoride henüz alışkanlık yok",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(habits) { habit ->
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
                        onClick = { /* Detay sayfasına git - Opsiyonel */ }
                    )
                }
            }

            // Bottom padding for FAB
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// --- KATEGORİYE ÖZEL ÖZET KARTLARI ---

@Composable
fun WaterSummaryCard(database: AppDatabase, onClick: () -> Unit) {
    // Basit veri çekme (ViewModel kullanmadan - Hızlı çözüm)
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val entries by database.waterDao().getEntriesByDate(today).collectAsState(initial = emptyList())

    // Toplam hesapla
    val totalAmount = entries.sumOf { it.amount }
    val targetAmount = 2500 // Varsayılan hedef (veya Prefs'ten alınabilir)
    val progress = (totalAmount.toFloat() / targetAmount).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), // Light Blue
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2196F3)),
                contentAlignment = Alignment.Center
            ) {
                Text("💧", fontSize = 24.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Su Takibi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF2196F3),
                    trackColor = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$totalAmount / $targetAmount ml",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1565C0)
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right), // Veya default icon
                contentDescription = null,
                tint = Color(0xFF1565C0)
            )
        }
    }
}

@Composable
fun NotesSummaryCard(database: AppDatabase, onClick: () -> Unit) {
    val notesCount by database.notesDao().getNoteCount().collectAsState(initial = 0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), // Light Orange
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF9800)),
                contentAlignment = Alignment.Center
            ) {
                Text("📝", fontSize = 24.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notlar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "$notesCount not kaydedildi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFEF6C00)
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = Color(0xFFEF6C00)
            )
        }
    }
}

@Composable
fun PomodoroSummaryCard(database: AppDatabase, onClick: () -> Unit) {
    // Pomodoro verisi olmadığı için statik gösteriyoruz
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), // Light Red
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF44336)),
                contentAlignment = Alignment.Center
            ) {
                Text("⏱️", fontSize = 24.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pomodoro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB71C1C)
                )
                Text(
                    text = "Odaklanma zamanı",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC62828)
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = Color(0xFFC62828)
            )
        }
    }
}

@Composable
fun FinanceSummaryCard(database: AppDatabase, onClick: () -> Unit) {
    // Finans verisi (basit bakiye)
    val transactions by database.transactionDao().getAllTransactions().collectAsState(initial = emptyList())

    // 🔥 DÜZELTME: "income" string'i yerine TransactionType.INCOME enum'ı kullanıldı
    val balance = transactions.sumOf {
        if (it.type == TransactionType.INCOME) it.amount else -it.amount
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), // Light Green
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Text("💰", fontSize = 24.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Finans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "Bakiye: ₺${balance}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (balance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right), // Drawable yoksa Icons.Default.ArrowForward kullan
                contentDescription = null,
                tint = Color(0xFF2E7D32)
            )
        }
    }
}