package com.example.naifdeneme.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.naifdeneme.PreferencesManager
import com.example.naifdeneme.R
import com.example.naifdeneme.database.AppDatabase
import com.example.naifdeneme.data.repository.HabitHubRepository
import com.example.naifdeneme.domain.model.HabitSource
import com.example.naifdeneme.domain.model.UnifiedHabit

@Composable
fun DashboardScreen(
    onNavigate: (HabitSource, Long?) -> Unit
) {
    val context = LocalContext.current

    // 🔥 Kullanıcı Adı
    val prefsManager = remember { PreferencesManager.getInstance(context) }
    val userName by prefsManager.userName.collectAsState(initial = stringResource(R.string.default_user_name))

    // Repository
    val database = AppDatabase.getDatabase(context)
    val repository = HabitHubRepository(
        context.applicationContext,
        database.habitDao(),
        database.waterDao(),
        database.pomodoroDao(),
        database.medicineDao()
    )

    val viewModel: DashboardViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(repository) as T
            }
        }
    )

    val habits by viewModel.dashboardState.collectAsState()

    Scaffold(
        topBar = { DashboardTopBar(userName) },
        bottomBar = { DashboardBottomBar(onNavigate) },
        // 🔥 Koyu Tema Arka Planı
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- 1. HEADER ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.dashboard_greeting, userName),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.dashboard_daily_quote),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }

            // --- 2. BAŞLIK ---
            item {
                Text(
                    text = stringResource(R.string.dashboard_tasks_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // --- 3. KARTLAR ---
            items(habits) { habit ->
                UnifiedHabitCard(
                    habit = habit,
                    onClick = { onNavigate(habit.source, habit.originalId) },
                    onQuickAction = { viewModel.onQuickAction(habit) }
                )
            }

            // --- 4. GRID BAŞLIĞI ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.grid_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // --- 5. GRID ---
            item {
                OverallStatusGrid(onNavigate)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun UnifiedHabitCard(
    habit: UnifiedHabit,
    onClick: () -> Unit,
    onQuickAction: () -> Unit
) {
    // 🔥 Koyu Tema Kart Renkleri
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(habit.color).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = habit.icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // 🔥 DİL ÇÖZÜMÜ: Eğer ID varsa onu çevir, yoksa düz yazıyı göster
                val titleText = if (habit.titleRes != null) stringResource(habit.titleRes!!) else habit.title

                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (habit.progress > 0f && habit.progress < 1f && !habit.isCompleted) {
                    LinearProgressIndicator(
                        progress = habit.progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(habit.color),
                        trackColor = Color(habit.color).copy(alpha = 0.15f)
                    )
                }

                Text(
                    text = habit.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (habit.actionLabel != null && !habit.isCompleted) {
                // 🔥 DİL ÇÖZÜMÜ: Buton için de aynısı
                val btnText = if (habit.actionLabelRes != null) stringResource(habit.actionLabelRes!!) else habit.actionLabel

                FilledTonalButton(
                    onClick = onQuickAction,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(habit.color).copy(alpha = 0.1f),
                        contentColor = Color(habit.color)
                    )
                ) {
                    Text(btnText ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else if (habit.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.status_completed),
                    tint = Color(habit.color),
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun OverallStatusGrid(onNavigate: (HabitSource, Long?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.grid_health),
                subtitle = stringResource(R.string.grid_health_desc),
                icon = Icons.Default.Favorite,
                color = Color(0xFFFFC2D1),
                iconColor = Color(0xFFD16D86),
                progress = 0.75f,
                onClick = { onNavigate(HabitSource.WATER, null) }
            )
            StatusCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.grid_education),
                subtitle = stringResource(R.string.grid_education_desc),
                icon = Icons.Default.School,
                color = Color(0xFFA3D5FF),
                iconColor = Color(0xFF4A8AC1),
                progress = 0.40f,
                onClick = { onNavigate(HabitSource.NOTES, null) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.grid_finance),
                subtitle = stringResource(R.string.grid_finance_desc),
                icon = Icons.Default.AccountBalanceWallet,
                color = Color(0xFFA2E4B8),
                iconColor = Color(0xFF42A562),
                progress = 0.60f,
                onClick = { onNavigate(HabitSource.FINANCE, null) }
            )
            StatusCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.grid_work),
                subtitle = stringResource(R.string.grid_work_desc),
                icon = Icons.Default.Work,
                color = Color(0xFFF0E68C),
                iconColor = Color(0xFFBCAE44),
                progress = 0.85f,
                onClick = { onNavigate(HabitSource.POMODORO, null) }
            )
        }
    }
}

@Composable
fun StatusCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    iconColor: Color,
    progress: Float,
    onClick: () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = iconColor,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun DashboardBottomBar(onNavigate: (HabitSource, Long?) -> Unit) {
    val containerColor = MaterialTheme.colorScheme.surface
    val selectedColor = MaterialTheme.colorScheme.primary

    NavigationBar(
        containerColor = containerColor,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = selectedColor,
                selectedTextColor = selectedColor,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { onNavigate(HabitSource.HABIT, null) },
            icon = { Icon(Icons.Default.TaskAlt, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_tasks)) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_analytics)) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { onNavigate(HabitSource.SETTINGS, null) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_settings)) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun DashboardTopBar(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            val initial = if (userName.isNotEmpty()) userName.take(1).uppercase() else "K"
            Text(initial, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

// Helper function
fun Modifier.drawTopBorder(color: Color = Color(0xFFEEEEEE)) = this.then(
    Modifier.padding(top = 1.dp).background(color).padding(top = 0.dp)
)