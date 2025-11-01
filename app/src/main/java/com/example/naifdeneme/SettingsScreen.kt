@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.naifdeneme

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naifdeneme.database.AppDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first


@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager.getInstance(context) }

    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }

    // Tema ve dil state'lerini burada yönetiyoruz
    var userName by remember { mutableStateOf("") }
    var isDarkMode by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("tr") }

    var showNameDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // DataStore'dan ayarları yükle ve dinle
    LaunchedEffect(Unit) {
        // İlk yükleme
        userName = prefsManager.getUserNameImmediate()
        isDarkMode = prefsManager.isDarkMode.first()
        language = prefsManager.language.first()

        // Real-time updates
        prefsManager.userName.collectLatest { name ->
            userName = name
        }
        prefsManager.isDarkMode.collectLatest { darkMode ->
            isDarkMode = darkMode
        }
        prefsManager.language.collectLatest { lang ->
            language = lang
        }
    }

    val testPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationHelper.scheduleTestReminder(context)
            scope.launch {
                snackbarHostState.showSnackbar("Test bildirimi 1 dakika sonra gelecek ⏰")
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Bildirim izni gerekli!")
            }
        }
    }

    LaunchedEffect(Unit) {
        NotificationHelper.createNotificationChannel(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.settings_back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Section(title = stringResource(R.string.settings_profile)) {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.settings_username),
                    subtitle = userName,
                    onClick = { showNameDialog = true }
                )
            }

            Section(title = "Görünüm") {
                SettingsItem(
                    icon = Icons.Default.Settings,
                    title = "Tema",
                    subtitle = if (isDarkMode) "Koyu" else "Açık",
                    onClick = { showThemeDialog = true }
                )

                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Dil",
                    subtitle = when (language) {
                        "tr" -> "Türkçe"
                        "en" -> "English"
                        else -> "Türkçe"
                    },
                    onClick = { showLanguageDialog = true }
                )
            }

            Section(title = stringResource(R.string.settings_app)) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_version),
                    subtitle = stringResource(R.string.settings_version_number),
                    onClick = null
                )

                SettingsItem(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.settings_rate_app),
                    subtitle = stringResource(R.string.settings_rate_app_desc),
                    onClick = { }
                )
            }

            Section(title = stringResource(R.string.settings_notifications)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Her alışkanlık için bildirim ayarlarını alışkanlık detay ekranından yapabilirsiniz.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🧪 Test Modu",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "1 dakika sonra test bildirimi",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (NotificationHelper.hasNotificationPermission(context)) {
                                        NotificationHelper.scheduleTestReminder(context)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Test bildirimi planlandı ⏰")
                                        }
                                    } else {
                                        testPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    NotificationHelper.scheduleTestReminder(context)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Test bildirimi planlandı ⏰")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Test", fontSize = 12.sp)
                        }
                    }
                }
            }

            Section(title = stringResource(R.string.settings_data)) {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = stringResource(R.string.settings_clear_all_data),
                    subtitle = stringResource(R.string.settings_clear_all_data_desc),
                    onClick = { showClearDataDialog = true },
                    textColor = MaterialTheme.colorScheme.error
                )
            }

            Section(title = stringResource(R.string.settings_about)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.app_name),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.app_tagline),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.settings_made_with_love),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showNameDialog) {
        NameDialog(
            currentName = userName,
            onDismiss = { showNameDialog = false },
            onSave = { newName ->
                scope.launch {
                    prefsManager.setUserName(newName)
                }
                showNameDialog = false
            }
        )
    }

    if (showThemeDialog) {
        ThemeDialog(
            currentTheme = isDarkMode,
            onDismiss = { showThemeDialog = false },
            onThemeChange = { isDarkMode ->
                scope.launch {
                    prefsManager.setDarkMode(isDarkMode)
                }
                showThemeDialog = false
            }
        )
    }

    if (showLanguageDialog) {
        LanguageDialog(
            currentLanguage = language,
            onDismiss = { showLanguageDialog = false },
            onLanguageChange = { newLanguage ->
                scope.launch {
                    prefsManager.setLanguage(newLanguage)
                }
                showLanguageDialog = false
            }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.clear_data_title)) },
            text = { Text(stringResource(R.string.clear_data_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            database.clearAllTables()
                            prefsManager.clearAll()
                            // State'leri sıfırla
                            userName = prefsManager.getUserNameImmediate()
                            isDarkMode = prefsManager.isDarkMode.first()
                            language = prefsManager.language.first()
                        }
                        showClearDataDialog = false
                    }
                ) {
                    Text(stringResource(R.string.settings_clear_data_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

// ThemeDialog ve LanguageDialog aynı kalıyor...
// [Aynı ThemeDialog, LanguageDialog, ThemeOption, LanguageOption, Section, SettingsItem, NameDialog kodları]

@Composable
fun ThemeDialog(
    currentTheme: Boolean,
    onDismiss: () -> Unit,
    onThemeChange: (Boolean) -> Unit
) {
    var selectedTheme by remember { mutableStateOf(currentTheme) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Settings, null) },
        title = { Text("Tema Seçimi") },
        text = {
            Column {
                ThemeOption(
                    title = "🌞 Açık Tema",
                    selected = !selectedTheme,
                    onClick = { selectedTheme = false }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ThemeOption(
                    title = "🌙 Koyu Tema",
                    selected = selectedTheme,
                    onClick = { selectedTheme = true }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onThemeChange(selectedTheme) }
            ) {
                Text("Uygula")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@Composable
fun LanguageDialog(
    currentLanguage: String,
    onDismiss: () -> Unit,
    onLanguageChange: (String) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, null) },
        title = { Text("Dil Seçimi") },
        text = {
            Column {
                LanguageOption(
                    title = "🇹🇷 Türkçe",
                    selected = selectedLanguage == "tr",
                    onClick = { selectedLanguage = "tr" }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LanguageOption(
                    title = "🇺🇸 English",
                    selected = selectedLanguage == "en",
                    onClick = { selectedLanguage = "en" }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onLanguageChange(selectedLanguage) }
            ) {
                Text("Uygula")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@Composable
fun ThemeOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun LanguageOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textColor)
                Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (onClick != null) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun NameDialog(currentName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Person, null) },
        title = { Text(stringResource(R.string.settings_name_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.settings_name_dialog_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        }
    )
}