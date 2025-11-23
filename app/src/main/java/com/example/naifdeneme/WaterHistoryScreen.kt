package com.example.naifdeneme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.naifdeneme.database.AppDatabase
import java.text.SimpleDateFormat
import java.util.*

/**
 * Su Geçmişi Ekranı
 * Günlük/Haftalık/Aylık istatistikler
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterHistoryScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current

    val viewModel: WaterViewModel = viewModel(
        factory = WaterViewModelFactory(
            waterDao = AppDatabase.getDatabase(context).waterDao(),
            preferencesManager = PreferencesManager.getInstance(context)
        )
    )

    var selectedPeriod by remember { mutableStateOf(HistoryPeriod.WEEKLY) }
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val targetAmount by viewModel.dailyTarget.collectAsState()

    val neonCyan = Color(0xFF06F9F9)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.water_history),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            HistoryPeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            HistorySummaryCard(
                weeklyStats = weeklyStats,
                targetAmount = targetAmount,
                period = selectedPeriod,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(24.dp))

            HistoryBarChart(
                weeklyStats = weeklyStats,
                targetAmount = targetAmount,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(24.dp))

            StatsCardsSection(
                weeklyStats = weeklyStats,
                targetAmount = targetAmount,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun HistoryPeriodSelector(
    selectedPeriod: HistoryPeriod,
    onPeriodSelected: (HistoryPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf(HistoryPeriod.DAILY, HistoryPeriod.WEEKLY, HistoryPeriod.MONTHLY)
    val neonCyan = Color(0xFF06F9F9)

    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.large
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            periods.forEach { period ->
                val isSelected = selectedPeriod == period
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = if (isSelected) neonCyan else Color.Transparent,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { onPeriodSelected(period) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(period.labelRes),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HistorySummaryCard(
    weeklyStats: List<DailyWaterStat>,
    targetAmount: Int,
    period: HistoryPeriod,
    modifier: Modifier = Modifier
) {
    val average = remember(weeklyStats) {
        if (weeklyStats.isEmpty()) 0 else weeklyStats.map { it.amount }.average().toInt()
    }

    val dateRange = remember(weeklyStats) {
        if (weeklyStats.isEmpty()) "" else {
            val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
            val first = weeklyStats.first().date
            val last = weeklyStats.last().date
            "${sdf.format(first)} - ${sdf.format(last)}"
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.weekly_average),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.ml_per_day, average),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HistoryBarChart(
    weeklyStats: List<DailyWaterStat>,
    targetAmount: Int,
    modifier: Modifier = Modifier
) {
    val neonCyan = Color(0xFF06F9F9)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyStats.forEach { stat ->
                    val percentage = if (targetAmount > 0) {
                        (stat.amount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .fillMaxHeight(percentage)
                                .background(color = neonCyan, shape = MaterialTheme.shapes.small)
                        )
                        Text(
                            text = stat.dayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCardsSection(
    weeklyStats: List<DailyWaterStat>,
    targetAmount: Int,
    modifier: Modifier = Modifier
) {
    val weeklyAverage = remember(weeklyStats) {
        if (weeklyStats.isEmpty()) 0 else weeklyStats.map { it.amount }.average().toInt()
    }

    val goalReachedDays = remember(weeklyStats, targetAmount) {
        weeklyStats.count { it.amount >= targetAmount }
    }

    val goalReachedPercentage = remember(weeklyStats, goalReachedDays) {
        if (weeklyStats.isEmpty()) 0 else ((goalReachedDays.toFloat() / weeklyStats.size) * 100).toInt()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.statistics),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = stringResource(R.string.weekly_average_label),
                value = stringResource(R.string.ml_per_day, weeklyAverage),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.goal_reached_days),
                value = "$goalReachedPercentage%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

enum class HistoryPeriod(val labelRes: Int) {
    DAILY(R.string.history_daily),
    WEEKLY(R.string.history_weekly),
    MONTHLY(R.string.history_monthly)
}