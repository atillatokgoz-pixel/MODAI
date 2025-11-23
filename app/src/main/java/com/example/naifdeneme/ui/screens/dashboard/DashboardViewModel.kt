package com.example.naifdeneme.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naifdeneme.data.repository.HabitHubRepository
import com.example.naifdeneme.domain.model.HabitSource
import com.example.naifdeneme.domain.model.UnifiedHabit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: HabitHubRepository
) : ViewModel() {

    val dashboardState: StateFlow<List<UnifiedHabit>> = repository.getDashboardHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onQuickAction(habit: UnifiedHabit) {
        viewModelScope.launch {
            when (habit.source) {
                HabitSource.WATER -> {
                    repository.addQuickWater(200)
                }
                // 🔥 ARTIK BURASI HATA VERMEZ
                HabitSource.HABIT -> {
                    if (habit.originalId != null) {
                        repository.toggleHabitCompletion(habit.originalId, habit.isCompleted)
                    }
                }
                else -> {}
            }
        }
    }
}