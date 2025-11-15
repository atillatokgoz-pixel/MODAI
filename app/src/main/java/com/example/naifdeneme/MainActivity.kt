package com.example.naifdeneme


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModaiTheme {
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
            HabitScreen(
                onNavigateToDetail = { habitId ->
                    navController.navigate("habit_detail/$habitId")
                }
                // onBack parametresi yok, bu yüzden kaldırıldı
            )
        }

        composable("water") {
            WaterTrackerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("settings") }
                // onNavigateToHistory optional, gerek yok
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

        // Detay Ekranları
        composable("habit_detail/{habitId}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")?.toLongOrNull() ?: 0L
            HabitDetailScreen(
                habitId = habitId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// Basit başlangıç ekranı (isteğe bağlı - test için)
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