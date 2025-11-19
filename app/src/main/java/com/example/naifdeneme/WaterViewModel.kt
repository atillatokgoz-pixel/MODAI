package com.example.naifdeneme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naifdeneme.database.WaterDao
import com.example.naifdeneme.database.WaterEntryEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Water Tracker ViewModel
 */
class WaterViewModel(
    private val waterDao: WaterDao,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // ============================================
    // STATE FLOWS
    // ============================================

    val dailyTarget: StateFlow<Int> = preferencesManager.waterDailyTarget
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 2500
        )

    val todayEntries: StateFlow<List<WaterEntryEntity>> = flow {
        val (startOfDay, endOfDay) = getTodayTimestamps()
        waterDao.getTodayEntries(startOfDay, endOfDay).collect { emit(it) }
    }.stateIn(
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

    val weeklyStats: StateFlow<List<DailyWaterStat>> = flow {
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

        emit(stats)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ============================================
    // ACTIONS
    // ============================================

    /**
     * 🔥 YENİ: Hatırlatıcı aç/kapa
     */
    fun updateReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setWaterReminderEnabled(enabled)
        }
    }

    fun addWater(amount: Int, note: String? = null) {
        viewModelScope.launch {
            val entry = WaterEntryEntity(
                amount = amount,
                timestamp = System.currentTimeMillis(),
                note = note
            )
            waterDao.insertEntry(entry)
        }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            waterDao.deleteEntry(entryId)
        }
    }

    fun updateDailyTarget(newTarget: Int) {
        viewModelScope.launch {
            preferencesManager.setWaterDailyTarget(newTarget)
        }
    }

    fun quickAddWater(amount: Int) {
        addWater(amount)
    }

    fun undoLastEntry() {
        viewModelScope.launch {
            val lastEntry = todayEntries.value.firstOrNull()
            lastEntry?.let { deleteEntry(it.id) }
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