package com.example.naifdeneme


import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.naifdeneme.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsManager = remember { PreferencesManager.getInstance(context) }
    val database = remember { AppDatabase.getDatabase(context) }

    val isDarkMode by prefsManager.isDarkMode.collectAsState(initial = false)
    val currentLanguage by prefsManager.language.collectAsState(initial = "tr")
    val userName by prefsManager.userName.collectAsState(initial = stringResource(R.string.default_user_name))

    var showNameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // --- 1. HESAP AYARLARI ---
            SettingsSection(title = "HESAP AYARLARI") {
                SettingsItem(
                    icon = Icons.Default.Person,
                    iconBgColor = Color(0xFFF4E2D0),
                    iconTint = Color(0xFFA2633A),
                    title = stringResource(R.string.settings_username),
                    subtitle = userName,
                    onClick = { showNameDialog = true }
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(start = 56.dp))
                SettingsItem(
                    icon = Icons.Default.Lock,
                    iconBgColor = Color(0xFFF4E2D0),
                    iconTint = Color(0xFFA2633A),
                    title = "Şifre",
                    onClick = { /* TODO */ }
                )
            }

            // --- 2. UYGULAMA TERCİHLERİ ---
            SettingsSection(title = "UYGULAMA TERCİHLERİ") {
                // Bildirimler
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Toggle Logic */ }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFA7D7F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, null, tint = Color(0xFF2E6A94))
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        stringResource(R.string.settings_notifications),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(checked = true, onCheckedChange = {})
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(start = 56.dp))

                // Dil Seçimi
                SettingsItem(
                    icon = Icons.Default.Language,
                    iconBgColor = Color(0xFFA7D7F9),
                    iconTint = Color(0xFF2E6A94),
                    title = stringResource(R.string.settings_language),
                    trailingContent = {
                        Text(
                            if (currentLanguage == "tr") "Türkçe" else "English",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    },
                    onClick = {
                        val newLang = if (currentLanguage == "tr") "en" else "tr"
                        scope.launch {
                            prefsManager.setLanguage(newLang)
                            (context as? Activity)?.recreate()
                        }
                    }
                )

                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(start = 56.dp))

                // Tema Seçimi
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFA7D7F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Contrast, null, tint = Color(0xFF2E6A94))
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        stringResource(R.string.settings_theme),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    // Segmented Control Benzeri Yapı
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            ThemeOptionBtn("Açık", !isDarkMode) { scope.launch { prefsManager.setDarkMode(false) } }
                            Spacer(Modifier.width(4.dp))
                            ThemeOptionBtn("Koyu", isDarkMode) { scope.launch { prefsManager.setDarkMode(true) } }
                        }
                    }
                }
            }

            // --- 3. GİZLİLİK ---
            SettingsSection(title = "GİZLİLİK VE GÜVENLİK") {
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    iconBgColor = Color(0xFFA2E4B8),
                    iconTint = Color(0xFF34754A),
                    title = "Gizlilik Politikası",
                    onClick = {}
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(start = 56.dp))
                SettingsItem(
                    icon = Icons.Default.Security,
                    iconBgColor = Color(0xFFA2E4B8),
                    iconTint = Color(0xFF34754A),
                    title = "Güvenlik Ayarları",
                    onClick = {}
                )
            }

            // --- 4. VERİ YÖNETİMİ ---
            SettingsSection(title = stringResource(R.string.settings_data)) {
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    iconBgColor = Color(0xFFF9C4C4),
                    iconTint = Color(0xFF9D4D4D),
                    title = stringResource(R.string.settings_clear_all_data),
                    onClick = { showDeleteDialog = true }
                )
            }

            // --- 5. ÇIKIŞ YAP ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().clickable { /* Logout */ }
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Çıkış Yap", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    // --- DIALOGS ---
    if (showNameDialog) {
        NameDialog(userName, { showNameDialog = false }, { name ->
            scope.launch { prefsManager.setUserName(name) }
            showNameDialog = false
        })
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.clear_data_title)) },
            text = { Text(stringResource(R.string.clear_data_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            database.clearAllTables()
                            prefsManager.clearAll()
                            (context as? Activity)?.recreate()
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.dialog_delete)) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.dialog_cancel)) } }
        )
    }
}

// --- COMPONENTS ---

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column { content() }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun ThemeOptionBtn(text: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
    val textColor = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
    val shadow = if (selected) 2.dp else 0.dp

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        shadowElevation = shadow
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = if(selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun NameDialog(currentName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kullanıcı Adı") },
        text = {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Adınız") }, singleLine = true
            )
        },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim()) }) { Text("Kaydet") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}