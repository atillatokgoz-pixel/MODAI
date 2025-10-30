package com.example.naifdeneme.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

data class HabitItem(
    val id: Int,
    val title: String,
    val progress: Float,
    val currentValue: String,
    val targetValue: String,
    val streak: Int = 0,
    val completionRate: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDashboardScreen(
    onSettingsClick: () -> Unit = {},
    onAddHabitClick: () -> Unit = {},
    onHabitClick: (HabitItem) -> Unit = {}
) {
    val habits = remember {
        mutableStateListOf(
            HabitItem(1, "💧 Su Takibi", 0.75f, "1.5L", "2L", streak = 5, completionRate = 75),
            HabitItem(2, "📚 Kitap Okuma", 0.6f, "18dk", "30dk", streak = 3, completionRate = 60),
            HabitItem(3, "🏋️ Spor", 0.4f, "12dk", "30dk", streak = 2, completionRate = 40),
            HabitItem(4, "🧘 Meditasyon", 0.9f, "9dk", "10dk", streak = 7, completionRate = 90)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Alışkanlıklarım",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onAddHabitClick) {
                        Icon(Icons.Default.Add, contentDescription = "Yeni Alışkanlık")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Bugünün Özeti",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(habits) { habit ->
                    SimpleHabitCard(
                        habit = habit,
                        onClick = { onHabitClick(habit) }
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleHabitCard(
    habit: HabitItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(150.dp)
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clickable { onClick() },
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = habit.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${habit.currentValue} / ${habit.targetValue}",
                style = MaterialTheme.typography.bodyMedium
            )

            LinearProgressIndicator(
                progress = habit.progress,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${habit.streak} gün",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "%${habit.completionRate}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Basit Water Screen
@Composable
fun SimpleWaterScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Su Takipçisi", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Hedef: 2000 ml", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Bugün: 500 ml", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = 0.25f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { /* su ekle */ }) {
            Text("+ 250 ml Ekle")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Geri")
        }
    }
}

// Basit Notes Screen
@Composable
fun SimpleNotesScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Notlar", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Basit not listesi
        Text("• Alışveriş listesi", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("• Toplantı notları", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("• Fikirler", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { /* yeni not */ }) {
            Text("+ Yeni Not")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Geri")
        }
    }
}

// Basit Settings Screen
@Composable
fun SimpleSettingsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Ayarlar", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Geri")
        }
    }
}

// Navigation fonksiyonu
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            SimpleDashboardScreen(
                onSettingsClick = { navController.navigate("settings") },
                onHabitClick = { navController.navigate("habits") },
                onWaterClick = { navController.navigate("water") },
                onNotesClick = { navController.navigate("notes") }  // ← Bunu ekle
            )
        }

        composable("habits") {
            HabitDashboardScreen(
                onSettingsClick = { navController.navigate("settings") },
                onAddHabitClick = { /* sonra ekleriz */ },
                onHabitClick = { /* sonra ekleriz */ }
            )
        }

        composable("water") {
            SimpleWaterScreen(onBack = { navController.popBackStack() })
        }

        composable("notes") {
            SimpleNotesScreen(onBack = { navController.popBackStack() })
        }

        composable("settings") {
            SimpleSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun SimpleDashboardScreen(
    onSettingsClick: () -> Unit,
    onHabitClick: () -> Unit,
    onWaterClick: () -> Unit,
    onNotesClick: () -> Unit  // ← Bu parametreyi ekle
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ana Sayfa", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onHabitClick, modifier = Modifier.fillMaxWidth()) {
            Text("Alışkanlıklar")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onWaterClick, modifier = Modifier.fillMaxWidth()) {
            Text("Su Takipçisi")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onNotesClick, modifier = Modifier.fillMaxWidth()) {  // ← Bu butonu ekle
            Text("Notlar")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onSettingsClick, modifier = Modifier.fillMaxWidth()) {
            Text("Ayarlar")
        }
    }
}