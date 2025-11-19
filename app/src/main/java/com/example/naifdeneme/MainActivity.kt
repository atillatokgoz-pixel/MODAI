package com.example.naifdeneme

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.naifdeneme.ui.screens.MainScreen
import com.example.naifdeneme.ui.screens.finance.FinanceScreen
import com.example.naifdeneme.ui.screens.water.WaterTrackerScreen
import com.example.naifdeneme.ui.theme.ModaiTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

class MainActivity : ComponentActivity() {

    // 🔥 DİL DEĞİŞİKLİĞİ İÇİN GEREKLİ
    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferencesManager.getInstance(newBase)
        val language = runBlocking { prefs.language.first() }
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 🔥 TEMA DEĞİŞİKLİĞİNİ DİNLE
            val prefsManager = remember { PreferencesManager.getInstance(this) }
            val isDarkMode by prefsManager.isDarkMode.collectAsState(initial = false)

            ModaiTheme(darkTheme = isDarkMode) {
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
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager.getInstance(context) }

    // 🔥 Son ekranı kontrol et (recreate sonrası geri dönmek için)
    val lastScreen by prefsManager.lastScreen.collectAsState(initial = "main")

    LaunchedEffect(lastScreen) {
        if (lastScreen != "main") {
            navController.navigate(lastScreen) {
                popUpTo("main") { inclusive = false }
            }
            prefsManager.clearLastScreen()
        }
    }

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // Ana Ekran
        composable("main") {
            MainScreen(
                onNavigateToHabits = { navController.navigate("habits") },
                onNavigateToWater = { navController.navigate("water") },
                onNavigateToFinance = { navController.navigate("finance") },
                onNavigateToNotes = { navController.navigate("notes") },
                onNavigateToPomodoro = { navController.navigate("pomodoro") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        // Modül Ekranları
        composable("habits") {
            HabitsScreen( // 🔥 DÜZELTİLDİ: HabitScreen → HabitsScreen
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("water") {
            WaterTrackerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToHistory = { navController.navigate("water_history") },
                onNavigateToReminderSettings = { navController.navigate("water_reminder_settings") } // 🔥 YENİ
            )
        }

        // 🔥 YENİ: Water History Screen
        composable("water_history") {
            WaterHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 🔥 YENİ: Water Reminder Settings Screen
        composable("water_reminder_settings") {
            WaterReminderSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("finance") {
            FinanceScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("notes") {
            NotesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("pomodoro") {
            PomodoroScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// 🔥 EKSİK COMPOSABLE FONKSİYONLARI EKLENDİ

@Composable
fun HabitsScreen(onNavigateBack: () -> Unit) {
    // Basit bir placeholder - gerçek implementasyon için HabitsScreen.kt gerekli
    WaterTrackerScreen(
        onNavigateBack = onNavigateBack,
        onNavigateToSettings = {},
        onNavigateToHistory = {},
        onNavigateToReminderSettings = {}
    )
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    MainScreen(
        onNavigateToHabits = { },
        onNavigateToWater = { },
        onNavigateToFinance = { },
        onNavigateToNotes = { },
        onNavigateToPomodoro = { },
        onNavigateToSettings = { }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ModaiTheme {
        Greeting("MODAI")
    }
}