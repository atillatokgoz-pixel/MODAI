package com.example.naifdeneme.ui.screens.habit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    prefillHabit: HabitEntity? = null,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val defaultCategory = prefillHabit?.category ?: "PERSONAL"

    var name by remember { mutableStateOf(prefillHabit?.name ?: "") }
    var selectedColor by remember { mutableStateOf(prefillHabit?.color ?: 0xFFFF6B6B) }
    var selectedIcon by remember { mutableStateOf(prefillHabit?.icon ?: "💪") }

    var selectedType by remember {
        mutableStateOf(prefillHabit?.type ?: HabitType.SIMPLE)
    }

    var targetValue by remember { mutableStateOf(prefillHabit?.targetValue?.toString() ?: "1") }
    var unit by remember { mutableStateOf(prefillHabit?.unit ?: "") }

    var isReminderEnabled by remember { mutableStateOf(prefillHabit?.reminderEnabled ?: false) }
    var reminderTime by remember { mutableStateOf(prefillHabit?.reminderTime ?: "09:00") }

    val icons = listOf("💪", "📚", "🧘", "💧", "🏃", "💊", "💰", "🧹", "🎵", "🍳", "😴", "📵")

    val colors = listOf(
        0xFFFF6B6B,
        0xFF4ECDC4,
        0xFF45B7D1,
        0xFF96CEB4,
        0xFFFFEEAD,
        0xFFD4A5A5,
        0xFF9B59B6,
        0xFFFF9800,
        0xFF2196F3
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (prefillHabit != null) "Şablonu Düzenle" else "Yeni Alışkanlık") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            CategoryBadge(defaultCategory)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Alışkanlık Adı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Text("Takip Tipi", fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

            AnimatedVisibility(visible = selectedType != HabitType.SIMPLE) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        if (selectedType == HabitType.COUNTABLE) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = targetValue,
                                    onValueChange = { if (it.all(Char::isDigit)) targetValue = it },
                                    label = { Text("Sayı") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = unit,
                                    onValueChange = { unit = it },
                                    label = { Text("Birim") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else if (selectedType == HabitType.TIMED) {
                            OutlinedTextField(
                                value = targetValue,
                                onValueChange = { if (it.all(Char::isDigit)) targetValue = it },
                                label = { Text("Dakika") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = { Text("dk") }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Hatırlatıcı")
                }
                Switch(
                    checked = isReminderEnabled,
                    onCheckedChange = { isReminderEnabled = it }
                )
            }

            Text("İkon Seç", fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                icons.forEach { icon ->
                    SelectableItem(
                        content = { Text(icon, fontSize = 24.sp) },
                        isSelected = selectedIcon == icon,
                        onClick = { selectedIcon = icon }
                    )
                }
            }

            Text("Renk Seç", fontWeight = FontWeight.Bold)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colors.forEach { colorVal ->
                    SelectableItem(
                        content = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorVal))
                            )
                        },
                        isSelected = selectedColor == colorVal,
                        onClick = { selectedColor = colorVal }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            val newHabit = HabitEntity(
                                name = name,
                                icon = selectedIcon,
                                color = selectedColor,
                                category = defaultCategory,
                                type = selectedType,
                                targetValue = targetValue.toIntOrNull() ?: 1,
                                unit = unit,
                                reminderEnabled = isReminderEnabled,
                                reminderTime = reminderTime,
                                currentProgress = 0,
                                frequency = "Daily",
                                priority = 1
                            )

                            database.habitDao().insertHabit(newHabit)
                            onSave()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = name.isNotBlank()
            ) {
                Text("Kaydet", fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}

@Composable
fun CategoryBadge(category: String) {
    val (text, color) = when (category) {
        "HEALTH" -> "Sağlık" to Color(0xFFFFC2D1)
        "WORK" -> "İş" to Color(0xFFF0E68C)
        "FINANCE" -> "Finans" to Color(0xFFA2E4B8)
        "EDUCATION" -> "Eğitim" to Color(0xFFA3D5FF)
        "FITNESS" -> "Fitness" to Color(0xFFFFD180)
        "PERSONAL" -> "Kişisel" to Color(0xFFE1BEE7)
        else -> "Genel" to Color.LightGray
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color)
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
        border = if (!isSelected) BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)) else null
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
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}
