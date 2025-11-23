package com.example.naifdeneme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naifdeneme.database.DrinkType
import com.example.naifdeneme.database.WaterDao
import com.example.naifdeneme.database.WaterEntryEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Water Tracker ViewModel
 * Version 3: Auto-refresh on date change
 */
class WaterViewModel(
    private val waterDao: WaterDao,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // ============================================
    // UI STATE
    // ============================================

    private val _uiState = MutableStateFlow<WaterUiState>(WaterUiState.Success)
    val uiState: StateFlow<WaterUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Tarih değişikliğini tetiklemek için
    private val _refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    // ============================================
    // STATE FLOWS
    // ============================================

    val dailyTarget: StateFlow<Int> = preferencesManager.waterDailyTarget
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 2500
        )

    // 🔥 GÜNCELLENDİ: refreshTrigger ile yeniden sorgu yapar
    val todayEntries: StateFlow<List<WaterEntryEntity>> = _refreshTrigger
        .flatMapLatest {
            val (startOfDay, endOfDay) = getTodayTimestamps()
            waterDao.getTodayEntries(startOfDay, endOfDay)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val reminderEnabled: StateFlow<Boolean> = preferencesManager.waterReminderEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val todayTotal: StateFlow<Int> = todayEntries
        .map { entries -> entries.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val progress: StateFlow<Float> = combine(todayTotal, dailyTarget) { total, target ->
        if (target > 0) (total.toFloat() / target.toFloat()).coerceIn(0f, 1f)
        else 0f
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    val isGoalReached: StateFlow<Boolean> = progress
        .map { it >= 1.0f }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val weeklyStats: StateFlow<List<DailyWaterStat>> = _refreshTrigger
        .flatMapLatest {
            flow { emit(calculateWeeklyStats()) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ============================================
    // ACTIONS
    // ============================================

    /**
     * Tarih değiştiğinde çağırılmalı
     */
    fun refreshDate() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun addWater(
        amount: Int,
        drinkType: DrinkType = DrinkType.WATER,
        note: String? = null
    ) {
        viewModelScope.launch {
            try {
                if (!validateAmount(amount)) return@launch

                _uiState.value = WaterUiState.Loading

                val entry = WaterEntryEntity(
                    amount = amount,
                    drinkType = drinkType.id,
                    drinkIcon = drinkType.emoji,
                    timestamp = System.currentTimeMillis(),
                    note = note?.trim()?.takeIf { it.isNotEmpty() }
                )

                waterDao.insertEntry(entry)
                _uiState.value = WaterUiState.Success
                clearError()

            } catch (e: Exception) {
                android.util.Log.e("WaterViewModel", "Error adding water", e)
                _uiState.value = WaterUiState.Error("Su eklenirken hata oluştu")
                _errorMessage.value = "Hata: ${e.localizedMessage}"
            }
        }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = WaterUiState.Loading
                waterDao.deleteEntry(entryId)
                _uiState.value = WaterUiState.Success
                clearError()
            } catch (e: Exception) {
                _uiState.value = WaterUiState.Error("Kayıt silinirken hata oluştu")
            }
        }
    }

    fun updateDailyTarget(newTarget: Int) {
        viewModelScope.launch {
            preferencesManager.setWaterDailyTarget(newTarget)
        }
    }

    fun updateReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setWaterReminderEnabled(enabled)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // ============================================
    // VALIDATION
    // ============================================

    private fun validateAmount(amount: Int): Boolean {
        return when {
            amount <= 0 -> {
                _errorMessage.value = "Miktar 0'dan büyük olmalı"
                false
            }
            amount > 5000 -> {
                _errorMessage.value = "Çok fazla! Maksimum 5000ml"
                false
            }
            else -> true
        }
    }

    // ============================================
    // HELPER FUNCTIONS
    // ============================================

    private fun getTodayTimestamps(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return startOfDay to endOfDay
    }

    private fun getTimestampsForDate(date: Date): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return startOfDay to endOfDay
    }

    private suspend fun calculateWeeklyStats(): List<DailyWaterStat> {
        val stats = mutableListOf<DailyWaterStat>()
        val calendar = Calendar.getInstance()

        repeat(7) { daysAgo ->
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)

            val (startOfDay, endOfDay) = getTimestampsForDate(calendar.time)
            val entries = waterDao.getTodayEntries(startOfDay, endOfDay).first()
            val total = entries.sumOf { it.amount }

            stats.add(0, DailyWaterStat(
                date = calendar.time,
                amount = total,
                dayName = getDayName(calendar.get(Calendar.DAY_OF_WEEK))
            ))
        }

        return stats
    }

    private fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.MONDAY -> "Pzt"
            Calendar.TUESDAY -> "Sal"
            Calendar.WEDNESDAY -> "Çar"
            Calendar.THURSDAY -> "Per"
            Calendar.FRIDAY -> "Cum"
            Calendar.SATURDAY -> "Cmt"
            Calendar.SUNDAY -> "Paz"
            else -> ""
        }
    }
}

/**
 * UI State
 */
sealed class WaterUiState {
    object Success : WaterUiState()
    object Loading : WaterUiState()
    data class Error(val message: String) : WaterUiState()
}

/**
 * Günlük su istatistiği
 */
data class DailyWaterStat(
    val date: Date,
    val amount: Int,
    val dayName: String
)

/**
 * ViewModel Factory
 */
class WaterViewModelFactory(
    private val waterDao: WaterDao,
    private val preferencesManager: PreferencesManager
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
            return WaterViewModel(waterDao, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}