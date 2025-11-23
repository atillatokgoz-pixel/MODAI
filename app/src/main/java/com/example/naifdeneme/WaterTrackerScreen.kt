package com.example.naifdeneme

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.naifdeneme.database.DrinkType
import com.example.naifdeneme.ui.components.CustomAmountDialog
import com.example.naifdeneme.ui.components.DrinkTypeSelector
import com.example.naifdeneme.ui.components.NoteInputDialog
import com.example.naifdeneme.ui.screens.water.components.AnimatedWaterProgressRing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToReminderSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: WaterViewModel = viewModel(
        factory = WaterViewModelFactory(
            waterDao = com.example.naifdeneme.database.AppDatabase.getDatabase(context).waterDao(),
            preferencesManager = PreferencesManager.getInstance(context)
        )
    )

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    // Ekran her öne geldiğinde tarihi yenile
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshDate()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // States
    val currentAmount by viewModel.todayTotal.collectAsState()
    val targetAmount by viewModel.dailyTarget.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isGoalReached by viewModel.isGoalReached.collectAsState()
    val reminderEnabled by viewModel.reminderEnabled.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // UI States
    var selectedAmount by remember { mutableStateOf(500) }
    var selectedDrinkType by remember { mutableStateOf(DrinkType.WATER) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    var showCustomAmountDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var pendingAmount by remember { mutableStateOf(0) }
    var pendingDrinkType by remember { mutableStateOf(DrinkType.WATER) }
    var buttonScale by remember { mutableStateOf(1f) }


    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.updateReminderEnabled(true)
            } else {
                // İzin verilmedi, kullanıcıya bilgi verilebilir
                viewModel.updateReminderEnabled(false)
            }
        }
    )

    // Celebration effect
    LaunchedEffect(isGoalReached) {
        if (isGoalReached && !showCelebration) {
            showCelebration = true
            delay(3000)
            showCelebration = false
        }
    }

    // Error handling
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    val neonCyan = Color(0xFF06F9F9)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.water_tracker_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_revert),
                            contentDescription = stringResource(R.string.cd_back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                            contentDescription = stringResource(R.string.water_history),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Ring
                Column(
                    modifier = Modifier.padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.clickable { showTargetDialog = true }
                    ) {
                        AnimatedWaterProgressRing(
                            currentAmount = currentAmount,
                            targetAmount = targetAmount,
                            animate = true
                        )
                    }

                    if (isGoalReached) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "🎉 " + stringResource(R.string.water_goal_reached),
                            style = MaterialTheme.typography.titleMedium,
                            color = neonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Reminder Toggle Card
                Card(
                    onClick = onNavigateToReminderSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(neonCyan.copy(alpha = 0.2f))
                                    .shadow(
                                        elevation = 2.dp,
                                        shape = CircleShape,
                                        spotColor = neonCyan.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = neonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = stringResource(R.string.reminders),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (reminderEnabled)
                                        stringResource(R.string.reminder_on)
                                    else
                                        stringResource(R.string.reminder_off),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    // Eğer Android 13+ ise ve izin yoksa iste
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        // Eski sürümse direkt aç
                                        viewModel.updateReminderEnabled(true)
                                    }
                                } else {
                                    viewModel.updateReminderEnabled(false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = neonCyan,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Drink Type Selector
                DrinkTypeSelector(
                    selectedType = selectedDrinkType,
                    onTypeSelected = { selectedDrinkType = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Amount Selector
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
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
                        listOf(250, 500, 750).forEach { amount ->
                            val isSelected = selectedAmount == amount
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        color = if (isSelected) neonCyan else Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .shadow(
                                        elevation = if (isSelected) 4.dp else 0.dp,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clickable { selectedAmount = amount },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${amount}ml",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        // Custom amount button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    color = Color.Transparent,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .clickable { showCustomAmountDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.custom_amount),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Add Button with Note Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Main Add Button
                    val animatedScale by animateFloatAsState(
                        targetValue = buttonScale,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        finishedListener = { buttonScale = 1f },
                        label = "button_scale"
                    )

                    Button(
                        onClick = {
                            viewModel.addWater(selectedAmount, selectedDrinkType, null)
                            buttonScale = 0.95f

                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.water_added_success, selectedAmount),
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .scale(animatedScale)
                            .shadow(
                                elevation = 12.dp,
                                shape = MaterialTheme.shapes.large,
                                spotColor = neonCyan.copy(alpha = 0.4f)
                            ),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = neonCyan
                        ),
                        enabled = uiState !is WaterUiState.Loading
                    ) {
                        if (uiState is WaterUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.add_water, selectedAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    // Add with Note Button
                    OutlinedButton(
                        onClick = {
                            pendingAmount = selectedAmount
                            pendingDrinkType = selectedDrinkType
                            showNoteDialog = true
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .width(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = neonCyan
                        ),
                        enabled = uiState !is WaterUiState.Loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = stringResource(R.string.add_note)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Daily Stats
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.daily_stats),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            val weeklyStats by viewModel.weeklyStats.collectAsState()
                            val last5Days = weeklyStats.takeLast(5)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                last5Days.forEach { stat ->
                                    val percentage = if (targetAmount > 0) {
                                        (stat.amount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f)
                                    } else 0f

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .fillMaxHeight(percentage)
                                                .clip(MaterialTheme.shapes.small)
                                                .shadow(
                                                    elevation = if (percentage > 0) 4.dp else 0.dp,
                                                    shape = MaterialTheme.shapes.small,
                                                    spotColor = if (percentage > 0) neonCyan.copy(alpha = 0.3f) else Color.Transparent
                                                )
                                                .background(
                                                    color = if (percentage > 0) neonCyan
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                )
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

                Spacer(modifier = Modifier.height(24.dp))

                // Recent Activities
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.recent_activities),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    val entries by viewModel.todayEntries.collectAsState()
                    val recentEntries = entries.take(5)

                    if (recentEntries.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.no_water_entries_today),
                                modifier = Modifier.padding(24.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentEntries.forEach { entry ->
                                val time = remember(entry.timestamp) {
                                    val calendar = java.util.Calendar.getInstance()
                                    calendar.timeInMillis = entry.timestamp
                                    String.format("%02d:%02d", calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE))
                                }

                                val drinkType = DrinkType.fromId(entry.drinkType)

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.large,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(neonCyan.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = drinkType.emoji,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = "${entry.amount} ml",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (!entry.note.isNullOrEmpty()) {
                                                    Text(
                                                        text = entry.note,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = time,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            // Delete button
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteEntry(entry.id)
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Kayıt silindi",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.delete_entry),
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Celebration Overlay
            if (showCelebration) {
                val infiniteTransition = rememberInfiniteTransition(label = "celebration")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { showCelebration = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎉 🎊 ✨\nTebrikler!\nGünlük Hedef Tamamlandı!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF06F9F9),
                        modifier = Modifier.scale(scale),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    // Target Dialog
    if (showTargetDialog) {
        var targetValue by remember { mutableStateOf(targetAmount) }
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_water_drop),
                    contentDescription = null,
                    tint = Color(0xFF06F9F9)
                )
            },
            title = { Text(stringResource(R.string.water_daily_target)) },
            text = {
                Column {
                    Text(stringResource(R.string.set_daily_target_desc))
                    Spacer(Modifier.height(16.dp))

                    Slider(
                        value = targetValue.toFloat(),
                        onValueChange = { targetValue = it.toInt() },
                        valueRange = 1000f..5000f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF06F9F9),
                            activeTrackColor = Color(0xFF06F9F9)
                        )
                    )

                    Text(
                        text = "$targetValue ml",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateDailyTarget(targetValue)
                    showTargetDialog = false
                }) {
                    Text(stringResource(R.string.dialog_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    // Custom Amount Dialog
    if (showCustomAmountDialog) {
        CustomAmountDialog(
            onDismiss = { showCustomAmountDialog = false },
            onConfirm = { amount ->
                selectedAmount = amount
                showCustomAmountDialog = false
            }
        )
    }

    // Note Input Dialog
    if (showNoteDialog) {
        NoteInputDialog(
            onDismiss = { showNoteDialog = false },
            onConfirm = { note ->
                viewModel.addWater(pendingAmount, pendingDrinkType, note)
                showNoteDialog = false

                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.water_added_success, pendingAmount),
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }
}