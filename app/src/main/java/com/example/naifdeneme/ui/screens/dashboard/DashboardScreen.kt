package com.example.naifdeneme.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.*
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
    // MainActivity'deki navigasyon yapısıyla uyumlu parametreler
    onNavigate: (HabitSource, Long?, String?) -> Unit
) {
    val context = LocalContext.current

    val prefsManager = remember { PreferencesManager.getInstance(context) }
    val userName by prefsManager.userName.collectAsState(
        initial = stringResource(R.string.default_user_name)
    )

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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                Spacer(Modifier.height(8.dp))
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

            item {
                Text(
                    text = stringResource(R.string.dashboard_tasks_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(habits) { habit ->
                UnifiedHabitCard(
                    habit = habit,
                    onClick = { onNavigate(habit.source, habit.originalId, habit.category) },
                    onQuickAction = { viewModel.onQuickAction(habit) }
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.grid_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                OverallStatusGrid(habits, onNavigate)
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun UnifiedHabitCard(
    habit: UnifiedHabit,
    onClick: () -> Unit,
    onQuickAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                val titleText =
                    if (habit.titleRes != null) stringResource(habit.titleRes!!)
                    else habit.title

                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(6.dp))

                if (habit.progress in 0f..0.99f) {
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

            Spacer(Modifier.width(12.dp))

            when {
                habit.actionLabel != null && !habit.isCompleted -> {
                    val btnText =
                        if (habit.actionLabelRes != null) stringResource(habit.actionLabelRes!!)
                        else habit.actionLabel

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
                }

                habit.isCompleted -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(habit.color),
                        modifier = Modifier.size(28.dp)
                    )
                }

                else -> {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun OverallStatusGrid(
    habits: List<UnifiedHabit>,
    onNavigate: (HabitSource, Long?, String?) -> Unit
) {
    val progressByCategory = remember(habits) {
        habits.groupBy { it.category ?: "OTHER" }
            .mapValues { (_, list) ->
                list.map { it.progress }.average().toFloat()
            }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.grid_health),
                subtitle = stringResource(R.string.grid_health_desc),
                icon = Icons.Default.Favorite,
                color = Color(0xFFFFC2D1),
                iconColor = Color(0xFFD16D86),
                progress = progressByCategory["HEALTH"] ?: 0f,
                // 🔥 GÜNCELLENDİ: Direkt Water yerine "HEALTH" Hub'ına gidiyor
                onClick = { onNavigate(HabitSource.HABIT, null, "HEALTH") }
            )

            StatusCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.grid_education),
                subtitle = stringResource(R.string.grid_education_desc),
                icon = Icons.Default.School,
                color = Color(0xFFA3D5FF),
                iconColor = Color(0xFF4A8AC1),
                progress = progressByCategory["EDUCATION"] ?: 0f,
                // 🔥 GÜNCELLENDİ: Direkt Notes yerine "EDUCATION" Hub'ına gidiyor
                onClick = { onNavigate(HabitSource.HABIT, null, "EDUCATION") }
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
                progress = progressByCategory["FINANCE"] ?: 0f,
                // 🔥 GÜNCELLENDİ: Direkt Finance yerine "FINANCE" Hub'ına gidiyor
                onClick = { onNavigate(HabitSource.HABIT, null, "FINANCE") }
            )

            StatusCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.grid_work),
                subtitle = stringResource(R.string.grid_work_desc),
                icon = Icons.Default.Work,
                color = Color(0xFFF0E68C),
                iconColor = Color(0xFFBCAE44),
                progress = progressByCategory["WORK"] ?: 0f,
                // 🔥 GÜNCELLENDİ: Direkt Pomodoro yerine "WORK" Hub'ına gidiyor
                onClick = { onNavigate(HabitSource.HABIT, null, "WORK") }
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
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            Modifier
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
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(6.dp))

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
fun DashboardBottomBar(
    onNavigate: (HabitSource, Long?, String?) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {

        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text(stringResource(R.string.nav_home)) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { onNavigate(HabitSource.HABIT, null, null) },
            icon = { Icon(Icons.Default.TaskAlt, null) },
            label = { Text(stringResource(R.string.nav_tasks)) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Analytics, null) },
            label = { Text(stringResource(R.string.nav_analytics)) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { onNavigate(HabitSource.SETTINGS, null, null) },
            icon = { Icon(Icons.Default.Settings, null) },
            label = { Text(stringResource(R.string.nav_settings)) }
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
            val initial = userName.take(1).uppercase()
            Text(
                initial,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}