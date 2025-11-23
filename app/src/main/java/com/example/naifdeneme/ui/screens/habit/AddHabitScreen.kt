package com.example.naifdeneme.ui.screens.habit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naifdeneme.database.AppDatabase
import com.example.naifdeneme.database.HabitEntity
import com.example.naifdeneme.database.HabitType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    initialCategory: String,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    // TEMEL BİLGİLER
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("💪") }
    var selectedColor by remember { mutableStateOf("#FF6B6B") }

    // 🔥 YENİ: HABIT TİPİ
    var selectedType by remember { mutableStateOf(HabitType.SIMPLE) }

    // 🔥 YENİ: HEDEF VE BİRİM
    var targetValue by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("") }

    // 🔥 YENİ: HATIRLATICI
    var isReminderEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf("09:00") } // Basitlik için String tutuyoruz

    val icons = listOf("💪", "📚", "🧘", "💧", "🏃", "💊", "💰", "🧹", "🎵", "🍳")
    val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEEAD", "#D4A5A5", "#9B59B6")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Alışkanlık Oluştur") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Geri") }
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. KATEGORİ BİLGİSİ (BİLGİ KARTI)
            CategoryBadge(initialCategory)

            // 2. İSİM GİRİŞİ
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Alışkanlık Adı (Örn: Kitap Oku)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 3. TİP SEÇİMİ (SEGMENTED CONTROL)
            Text("Takip Tipi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TypeOption(
                    title = "Basit",
                    icon = Icons.Default.CheckCircle,
                    isSelected = selectedType == HabitType.SIMPLE,
                    onClick = { selectedType = HabitType.SIMPLE }
                )
                TypeOption(
                    title = "Hedefli",
                    icon = Icons.Default.FormatListNumbered,
                    isSelected = selectedType == HabitType.COUNTABLE,
                    onClick = { selectedType = HabitType.COUNTABLE }
                )
                TypeOption(
                    title = "Süreli",
                    icon = Icons.Default.Timer,
                    isSelected = selectedType == HabitType.TIMED,
                    onClick = { selectedType = HabitType.TIMED }
                )
            }

            // 4. DİNAMİK ALANLAR (SEÇİME GÖRE AÇILIR)
            AnimatedVisibility(visible = selectedType != HabitType.SIMPLE) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (selectedType == HabitType.COUNTABLE) {
                            Text("Hedef Belirle", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = targetValue,
                                    onValueChange = { if (it.all { char -> char.isDigit() }) targetValue = it },
                                    label = { Text("Sayı") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = unit,
                                    onValueChange = { unit = it },
                                    label = { Text("Birim (Sayfa, Bardak)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        } else if (selectedType == HabitType.TIMED) {
                            Text("Süre Belirle (Dakika)", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = targetValue,
                                onValueChange = { if (it.all { char -> char.isDigit() }) targetValue = it },
                                label = { Text("Dakika") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = { Text("dk", modifier = Modifier.padding(end = 16.dp)) },
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // 5. HATIRLATICI (SWITCH)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Hatırlatıcı", style = MaterialTheme.typography.titleMedium)
                }
                Switch(
                    checked = isReminderEnabled,
                    onCheckedChange = { isReminderEnabled = it }
                )
            }

            // 6. İKON SEÇİMİ
            Text("İkon Seç", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                icons.forEach { icon ->
                    SelectableItem(
                        content = { Text(icon, fontSize = 24.sp) },
                        isSelected = selectedIcon == icon,
                        onClick = { selectedIcon = icon }
                    )
                }
            }

            // 7. RENK SEÇİMİ
            Text("Renk Seç", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                colors.forEach { colorHex ->
                    SelectableItem(
                        content = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(colorHex)))
                            )
                        },
                        isSelected = selectedColor == colorHex,
                        onClick = { selectedColor = colorHex }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // KAYDET BUTONU
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            val newHabit = HabitEntity(
                                name = name,
                                icon = selectedIcon,
                                color = selectedColor,
                                category = initialCategory,
                                type = selectedType,
                                targetValue = targetValue.toIntOrNull() ?: 1,
                                unit = if(selectedType == HabitType.TIMED) "dk" else unit,
                                reminderEnabled = isReminderEnabled
                            )
                            database.habitDao().insertHabit(newHabit)
                            onSave()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = name.isNotBlank()
            ) {
                Text("Kaydet", fontSize = 16.sp)
            }
        }
    }
}

// --- YARDIMCI COMPONENTLER ---

@Composable
fun CategoryBadge(category: String) {
    val (text, color) = when(category) {
        "HEALTH" -> "Sağlık" to Color(0xFFFFC2D1)
        "WORK" -> "İş" to Color(0xFFF0E68C)
        "FINANCE" -> "Finans" to Color(0xFFA2E4B8)
        "EDUCATION" -> "Eğitim" to Color(0xFFA3D5FF)
        else -> "Genel" to Color.LightGray
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Category, null, modifier = Modifier.size(16.dp), tint = Color.Black)
            Spacer(Modifier.width(4.dp))
            Text(text, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun RowScope.TypeOption(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .weight(1f)
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)) else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = contentColor)
            Spacer(Modifier.height(4.dp))
            Text(title, color = contentColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SelectableItem(
    content: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent, CircleShape)
                    .clickable(onClick = onClick)
            ) {
                // Seçili olduğunu belirten minik işaret (Opsiyonel)
            }
        }
    }
}