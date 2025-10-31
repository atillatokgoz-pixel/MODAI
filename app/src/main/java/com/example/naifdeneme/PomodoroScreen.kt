package com.example.naifdeneme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naifdeneme.database.*
import com.example.naifdeneme.ui.components.CardLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * PomodoroScreen - Pomodoro zamanlayıcı ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val pomodoroDao = remember { database.pomodoroDao() }
    val scope = rememberCoroutineScope()

    // Timer state
    var timeLeft by remember { mutableStateOf(25 * 60) } // 25 dakika (saniye)
    var isRunning by remember { mutableStateOf(false) }
    var isWorkSession by remember { mutableStateOf(true) } // true = çalışma, false = mola
    var sessionStartTime by remember { mutableStateOf(0L) }

    // Bugünkü seans sayısı
    val startOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val endOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    val todaySessionCount by pomodoroDao.getTodayWorkSessionCount(startOfDay, endOfDay)
        .collectAsState(initial = 0)

    // Timer countdown - DÜZELTİLDİ
    LaunchedEffect(isRunning) {
        while (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft = timeLeft - 1

            // Süre bitti
            if (timeLeft <= 0) {
                isRunning = false
                timeLeft = 0

                // Seansı kaydet
                scope.launch {
                    val currentTime = System.currentTimeMillis()
                    pomodoroDao.insertSession(
                        PomodoroEntity(
                            startTime = sessionStartTime,
                            endTime = currentTime,
                            duration = if (isWorkSession) 25 else 5,
                            type = if (isWorkSession) PomodoroType.WORK else PomodoroType.BREAK,
                            completed = true
                        )
                    )
                }

                // Otomatik geçiş (Çalışma → Mola, Mola → Çalışma)
                isWorkSession = !isWorkSession
                timeLeft = if (isWorkSession) 25 * 60 else 5 * 60
            }
        }
    }

    // Animation scale
    val scale by animateFloatAsState(
        targetValue = if (isRunning) 1.05f else 1f,
        animationSpec = if (isRunning) {
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(durationMillis = 300)
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.pomodoro_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.finance_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Bugünkü seans sayısı
            CardLight(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎯 ${stringResource(R.string.pomodoro_title)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$todaySessionCount ${stringResource(R.string.pomodoro_work_time)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tab sistemi (Çalışma / Mola)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Çalışma tab
                FilterChip(
                    selected = isWorkSession,
                    onClick = {
                        if (!isRunning) {
                            isWorkSession = true
                            timeLeft = 25 * 60
                        }
                    },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💪", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.pomodoro_work_time),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = !isRunning
                )

                // Mola tab
                FilterChip(
                    selected = !isWorkSession,
                    onClick = {
                        if (!isRunning) {
                            isWorkSession = false
                            timeLeft = 5 * 60
                        }
                    },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "☕", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.pomodoro_break_time),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = !isRunning
                )
            }

            // Timer circle
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(scale)
                    .background(
                        color = if (isWorkSession)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Timer display
                    Text(
                        text = formatTime(timeLeft),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWorkSession)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress text
                    val totalSeconds = if (isWorkSession) 25 * 60 else 5 * 60
                    val progress = if (totalSeconds > 0) {
                        ((totalSeconds - timeLeft).toFloat() / totalSeconds * 100).toInt()
                    } else {
                        0
                    }

                    Text(
                        text = "%$progress",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 🆕 HIZLI SÜRE BUTONLARI - Timer circle altına ekle
            if (!isRunning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickTimeButton(
                        text = "15 dk",
                        minutes = 15,
                        isWorkSession = isWorkSession,
                        onClick = { minutes -> timeLeft = minutes * 60 }
                    )
                    QuickTimeButton(
                        text = "25 dk",
                        minutes = 25,
                        isWorkSession = isWorkSession,
                        onClick = { minutes -> timeLeft = minutes * 60 }
                    )
                    QuickTimeButton(
                        text = "45 dk",
                        minutes = 45,
                        isWorkSession = isWorkSession,
                        onClick = { minutes -> timeLeft = minutes * 60 }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start/Stop button - ÖZEL İKON
                Button(
                    onClick = {
                        if (!isRunning) {
                            // Başlat
                            if (timeLeft > 0) {
                                sessionStartTime = System.currentTimeMillis()
                            }
                            isRunning = true
                        } else {
                            // Durdur
                            isRunning = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning)
                            MaterialTheme.colorScheme.error
                        else if (isWorkSession)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    // Özel ikon container'ı
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (isRunning) {
                                // Durdur ikonu - iki dikey dikdörtgen
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(size.width * 0.25f, size.height * 0.2f),
                                    size = Size(size.width * 0.15f, size.height * 0.6f)
                                )
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(size.width * 0.6f, size.height * 0.2f),
                                    size = Size(size.width * 0.15f, size.height * 0.6f)
                                )
                            } else {
                                // Oynat ikonu - üçgen
                                val path = Path().apply {
                                    moveTo(size.width * 0.3f, size.height * 0.2f)
                                    lineTo(size.width * 0.3f, size.height * 0.8f)
                                    lineTo(size.width * 0.8f, size.height * 0.5f)
                                    close()
                                }
                                drawPath(
                                    path = path,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning)
                            stringResource(R.string.pomodoro_stop)
                        else
                            stringResource(R.string.pomodoro_start),
                        fontSize = 16.sp
                    )
                }

                // Reset button
                OutlinedButton(
                    onClick = {
                        isRunning = false
                        timeLeft = if (isWorkSession) 25 * 60 else 5 * 60
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.pomodoro_reset),
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch session type button
            TextButton(
                onClick = {
                    if (!isRunning) {
                        isWorkSession = !isWorkSession
                        timeLeft = if (isWorkSession) 25 * 60 else 5 * 60
                    }
                },
                enabled = !isRunning
            ) {
                Text(
                    text = "↔️ ${if (isWorkSession)
                        stringResource(R.string.pomodoro_break_time)
                    else
                        stringResource(R.string.pomodoro_work_time)
                    }",
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 🆕 Hızlı süre butonu component'i
 */
@Composable
fun QuickTimeButton(
    text: String,
    minutes: Int,
    isWorkSession: Boolean,
    onClick: (Int) -> Unit
) {
    TextButton(
        onClick = { onClick(minutes) },
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (isWorkSession)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
            contentColor = if (isWorkSession)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.tertiary
        )
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Saniyeyi MM:SS formatına çevirir
 */
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}