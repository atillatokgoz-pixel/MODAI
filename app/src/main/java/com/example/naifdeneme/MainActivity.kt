package com.example.naifdeneme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.naifdeneme.ui.screens.finance.FinanceScreen
import com.example.naifdeneme.ui.screens.water.WaterTrackerScreen
import com.example.naifdeneme.ui.theme.ModaiTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Tema ve dil state'ini burada yönetiyoruz
            val preferencesManager = remember { PreferencesManager.getInstance(this) }

            // Tema state'leri
            val darkThemeState = remember { mutableStateOf(false) }
            val languageState = remember { mutableStateOf("tr") }
            val dynamicColorState = remember { mutableStateOf(true) }

            // DataStore'dan ayarları yükle
            LaunchedEffect(Unit) {
                preferencesManager.isDarkMode.collectLatest { isDark ->
                    darkThemeState.value = isDark
                }
            }
            LaunchedEffect(Unit) {
                preferencesManager.language.collectLatest { lang ->
                    languageState.value = lang
                }
            }
            LaunchedEffect(Unit) {
                preferencesManager.dynamicColor.collectLatest { dynamic ->
                    dynamicColorState.value = dynamic
                }
            }

            ModaiTheme(
                darkTheme = darkThemeState.value,
                dynamicColor = dynamicColorState.value
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

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
                onNotesClick = { navController.navigate("notes") },
                onFinanceClick = { navController.navigate("finance") },
                onPomodoroClick = { navController.navigate("pomodoro") },
                onWaterClick = { navController.navigate("water") }
            )
        }

        composable("habits") {
            HabitScreen(onNavigateToDetail = { habitId ->
                // TODO: Habit detail ekranına yönlendirme
            })
        }

        composable("notes") {
            NotesScreen(onBack = { navController.popBackStack() })
        }

        composable("finance") {
            FinanceScreen(onBack = { navController.popBackStack() })
        }

        composable("pomodoro") {
            PomodoroScreen(onBack = { navController.popBackStack() })
        }

        composable("water") {
            WaterTrackerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToHistory = { /* sonra ekleriz */ }
            )
        }

        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

// BASIT EKRANLAR - Hepsi MainActivity.kt içinde
@Composable
fun SimpleDashboardScreen(
    onSettingsClick: () -> Unit,
    onHabitClick: () -> Unit,
    onNotesClick: () -> Unit,
    onFinanceClick: () -> Unit,
    onPomodoroClick: () -> Unit,
    onWaterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MODAI",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "AI Destekli Yaşam Yönetimi",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Modül butonları - Tema renkleriyle uyumlu
        ModuleButton(
            text = "💪 Alışkanlıklar",
            onClick = onHabitClick,
            color = MaterialTheme.colorScheme.primary
        )

        ModuleButton(
            text = "📝 Notlar",
            onClick = onNotesClick,
            color = MaterialTheme.colorScheme.secondary
        )

        ModuleButton(
            text = "💰 Finans",
            onClick = onFinanceClick,
            color = MaterialTheme.colorScheme.tertiary
        )

        ModuleButton(
            text = "⏰ Pomodoro",
            onClick = onPomodoroClick,
            color = MaterialTheme.colorScheme.primary
        )

        ModuleButton(
            text = "💧 Su Takipçisi",
            onClick = onWaterClick,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        ModuleButton(
            text = "⚙️ Ayarlar",
            onClick = onSettingsClick,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun ModuleButton(
    text: String,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SimpleNotesScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Notlar",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Geri")
        }
    }
}

@Composable
fun SimpleFinanceScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Finans",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Geri")
        }
    }
}

@Composable
fun SimplePomodoroScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Pomodoro",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Geri")
        }
    }
}

@Composable
fun SimpleSettingsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Ayarlar",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Geri")
        }
    }
}