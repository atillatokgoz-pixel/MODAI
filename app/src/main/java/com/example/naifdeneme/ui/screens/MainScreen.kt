@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.naifdeneme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.naifdeneme.SettingsScreen
import java.util.*


@Composable
fun MainScreen(
    onNavigateToHabits: () -> Unit = {},
    onNavigateToWater: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToPomodoro: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MODAI",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    BottomNavItem("Ana Sayfa", Icons.Filled.Home, 0),
                    BottomNavItem("Modüller", Icons.Filled.ViewModule, 1),
                    BottomNavItem("İstatistikler", Icons.Filled.BarChart, 2),
                    BottomNavItem("Ayarlar", Icons.Filled.Settings, 3)
                )

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedTab == item.index,
                        onClick = { selectedTab = item.index }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> DashboardContent(
                paddingValues = paddingValues,
                onNavigateToHabits = onNavigateToHabits,
                onNavigateToWater = onNavigateToWater,
                onNavigateToFinance = onNavigateToFinance,
                onNavigateToNotes = onNavigateToNotes,
                onNavigateToPomodoro = onNavigateToPomodoro
            )
            1 -> ModulesContent(
                paddingValues = paddingValues,
                onNavigateToHabits = onNavigateToHabits,
                onNavigateToWater = onNavigateToWater,
                onNavigateToFinance = onNavigateToFinance,
                onNavigateToNotes = onNavigateToNotes,
                onNavigateToPomodoro = onNavigateToPomodoro
            )
            2 -> StatisticsContent(paddingValues)
            3 -> SettingsContent(paddingValues)
        }
    }
}

@Composable
fun DashboardContent(
    paddingValues: PaddingValues,
    onNavigateToHabits: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToPomodoro: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { GreetingSection() }
        item { QuickStatsSection() }
        item {
            ModuleCardsSection(
                onHabitClick = onNavigateToHabits,
                onWaterClick = onNavigateToWater,
                onFinanceClick = onNavigateToFinance,
                onNotesClick = onNavigateToNotes,
                onPomodoroClick = onNavigateToPomodoro
            )
        }
    }
}

@Composable
fun ModulesContent(
    paddingValues: PaddingValues,
    onNavigateToHabits: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToPomodoro: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Tüm Modüller",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        item {
            ModuleCardsSection(
                onHabitClick = onNavigateToHabits,
                onWaterClick = onNavigateToWater,
                onFinanceClick = onNavigateToFinance,
                onNotesClick = onNavigateToNotes,
                onPomodoroClick = onNavigateToPomodoro
            )
        }
    }
}

@Composable
fun StatisticsContent(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📊 İstatistikler Yakında Gelecek",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Bu özellik yakında eklenecek",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun SettingsContent(paddingValues: PaddingValues) {
    Box(modifier = Modifier.padding(paddingValues)) {
        SettingsScreen(onBack = { })
    }
}

@Composable
fun GreetingSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Merhaba, Kullanıcı! 👋",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        val currentTime = System.currentTimeMillis()
        val date = java.text.SimpleDateFormat("d MMMM yyyy", Locale("tr"))
            .format(currentTime)

        Text(
            text = "📅 $date",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickStatsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("✅ 5", "Tamamlanan")
            StatItem("🔄 3", "Devam Eden")
            StatItem("📈 75%", "Başarı")
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ModuleCardsSection(
    onHabitClick: () -> Unit = {},
    onWaterClick: () -> Unit = {},
    onFinanceClick: () -> Unit = {},
    onNotesClick: () -> Unit = {},
    onPomodoroClick: () -> Unit = {}
) {
    val modules = listOf(
        ModuleCard("💪", "Alışkanlıklar", "Günlük hedeflerini takip et", onHabitClick),
        ModuleCard("📝", "Notlar", "Fikirlerini kaydet", onNotesClick),
        ModuleCard("💰", "Finans", "Gelir ve giderlerini yönet", onFinanceClick),
        ModuleCard("⏰", "Pomodoro", "Odaklanmak için zamanlayıcı", onPomodoroClick),
        ModuleCard("💧", "Su Takipçisi", "Günlük su hedefini takip et", onWaterClick),
        ModuleCard("🤖", "AI Coach", "Kişisel AI asistanın", {})
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(400.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(modules) { module ->
            ModuleCardItem(module)
        }
    }
}

@Composable
fun ModuleCardItem(module: ModuleCard) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = module.onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = module.emoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = module.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = module.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)

data class ModuleCard(
    val emoji: String,
    val title: String,
    val description: String,
    val onClick: () -> Unit
)