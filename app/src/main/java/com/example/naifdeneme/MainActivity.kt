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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.naifdeneme.database.HabitEntity
import com.example.naifdeneme.database.HabitTemplates
import com.example.naifdeneme.database.HabitType
import com.example.naifdeneme.domain.model.HabitSource
import com.example.naifdeneme.ui.screens.dashboard.DashboardScreen
import com.example.naifdeneme.ui.screens.finance.FinanceScreen
import com.example.naifdeneme.ui.screens.habit.AddHabitScreen
import com.example.naifdeneme.ui.screens.habit.CategoryHabitScreen
import com.example.naifdeneme.ui.screens.habit.CategoryHubScreen
import com.example.naifdeneme.ui.screens.habit.TemplateSelectionScreen
import com.example.naifdeneme.ui.screens.medicine.MedicineScreen
import com.example.naifdeneme.ui.theme.ModaiTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

class MainActivity : ComponentActivity() {

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

        val startDestination = intent?.getStringExtra("navigate_to") ?: "main"

        setContent {
            val prefsManager = remember { PreferencesManager.getInstance(this) }
            val isDarkMode by prefsManager.isDarkMode.collectAsState(initial = false)

            ModaiTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(startDestination = startDestination)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(startDestination: String = "main") {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager.getInstance(context) }

    val lastScreen by prefsManager.lastScreen.collectAsState(initial = "main")
    val initialRoute = if (startDestination != "main") startDestination else lastScreen

    LaunchedEffect(initialRoute) {
        if (initialRoute != "main" && initialRoute != startDestination) {
            navController.navigate(initialRoute) {
                popUpTo("main") { inclusive = false }
            }
            prefsManager.clearLastScreen()
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (startDestination != "main") startDestination else "main"
    ) {

        // DASHBOARD
        composable("main") {
            DashboardScreen(
                onNavigate = { source, id, category ->
                    when (source) {
                        HabitSource.WATER -> navController.navigate("water")
                        HabitSource.POMODORO -> navController.navigate("pomodoro")
                        HabitSource.FINANCE -> navController.navigate("finance")
                        HabitSource.NOTES -> navController.navigate("notes")
                        HabitSource.SETTINGS -> navController.navigate("settings")
                        HabitSource.MEDICINE -> navController.navigate("medicine")

                        HabitSource.HABIT -> {
                            when {
                                category != null -> {
                                    // 🔥 GÜNCELLENDİ: Artık Hub ekranına gidiyor
                                    navController.navigate("category_hub/$category")
                                }
                                id != null -> {
                                    // BELİRLİ ALIŞKANLIK DETAYI
                                    navController.navigate("habit_detail/$id")
                                }
                                else -> {
                                    // GENEL ALIŞKANLIK / ŞABLON SEÇİMİ
                                    navController.navigate("habit_templates")
                                }
                            }
                        }
                    }
                }
            )
        }

        // 🔥 YENİ: CATEGORY HUB SCREEN (Akıllı Kategori Ekranı)
        composable(
            route = "category_hub/{category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("category") ?: "PERSONAL"

            CategoryHubScreen(
                categoryName = categoryName,
                onBack = { navController.popBackStack() },
                onNavigateToModule = { moduleRoute ->
                    navController.navigate(moduleRoute)
                },
                onAddHabit = { cat ->
                    navController.navigate("add_habit/$cat")
                }
            )
        }

        // ŞABLON SEÇİM EKRANI
        composable("habit_templates") {
            TemplateSelectionScreen(
                onTemplateSelected = { template ->
                    navController.navigate("add_habit_from_template/${template.id}")
                },
                onCustomClick = {
                    navController.navigate("add_habit/PERSONAL")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ŞABLONDAN HABIT EKLEME
        composable(
            route = "add_habit_from_template/{templateId}",
            arguments = listOf(navArgument("templateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId")
            val template = HabitTemplates.ALL.find { it.id == templateId }

            val prefillEntity = template?.let { t ->
                HabitEntity(
                    name = t.name,
                    description = t.description,
                    icon = t.icon,
                    color = t.color,
                    category = t.category.name,
                    type = when (t.type) {
                        // Model'deki Enum'ı Database'deki Enum'a çeviriyoruz
                        com.example.naifdeneme.model.HabitType.SIMPLE -> HabitType.SIMPLE
                        com.example.naifdeneme.model.HabitType.COUNTABLE -> HabitType.COUNTABLE
                        com.example.naifdeneme.model.HabitType.TIMED -> HabitType.TIMED
                    },
                    targetValue = t.targetValue ?: 1,
                    unit = t.unit ?: "adet",
                    currentProgress = 0,
                    frequency = "Daily",
                    priority = 1
                )
            }

            AddHabitScreen(
                prefillHabit = prefillEntity,
                onBack = { navController.popBackStack() },
                onSave = {
                    // Kaydettikten sonra ana ekrana dön
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }

        // KATEGORİYE GÖRE ELLE EKLEME
        composable(
            route = "add_habit/{category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "PERSONAL"

            val categoryPrefill = HabitEntity(
                name = "",
                icon = "💪",
                color = 0xFF6B5CE7,
                category = category,
                type = HabitType.SIMPLE,
                targetValue = 1,
                unit = "",
                currentProgress = 0,
                frequency = "Daily",
                priority = 1
            )

            AddHabitScreen(
                prefillHabit = categoryPrefill,
                onBack = { navController.popBackStack() },
                onSave = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }

        // HABIT DETAY
        composable(
            route = "habit_detail/{habitId}",
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: 0L
            HabitDetailScreen(
                habitId = habitId,
                onBack = { navController.popBackStack() }
            )
        }

        // KATEGORİ ALIŞKANLIK EKRANI (Eski yöntem, ama hala linklenebilir)
        composable(
            route = "category_habits/{category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "OTHER"
            CategoryHabitScreen(
                category = category,
                onBack = { navController.popBackStack() },
                onAddHabit = { cat -> navController.navigate("add_habit/$cat") }
            )
        }

        // DİĞER MODÜLLER

        composable("habits") {
            HabitScreen(onNavigateToDetail = { id -> navController.navigate("habit_detail/$id") })
        }

        composable("water") {
            WaterTrackerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToHistory = { navController.navigate("water_history") },
                onNavigateToReminderSettings = { navController.navigate("water_reminder_settings") }
            )
        }

        composable("water_history") {
            WaterHistoryScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("water_reminder_settings") {
            WaterReminderSettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("finance") {
            FinanceScreen(onBack = { navController.popBackStack() })
        }

        composable("notes") {
            NotesScreen(onBack = { navController.popBackStack() })
        }

        composable("pomodoro") {
            PomodoroScreen(onBack = { navController.popBackStack() })
        }

        // 🔥 GÜNCELLENDİ: Medicine Screen
        composable("medicine") {
            MedicineScreen(onBack = { navController.popBackStack() })
        }

        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}