package com.example.naifdeneme.data.repository

import android.content.Context
import com.example.naifdeneme.R
import com.example.naifdeneme.database.HabitDao
import com.example.naifdeneme.database.MedicineDao
import com.example.naifdeneme.database.PomodoroDao
import com.example.naifdeneme.database.WaterDao
import com.example.naifdeneme.database.WaterEntryEntity
import com.example.naifdeneme.domain.model.HabitSource
import com.example.naifdeneme.domain.model.UnifiedHabit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import java.util.UUID

class HabitHubRepository(
    private val context: Context,
    private val habitDao: HabitDao,
    private val waterDao: WaterDao,
    private val pomodoroDao: PomodoroDao,
    private val medicineDao: MedicineDao
) {

    fun getDashboardHabits(): Flow<List<UnifiedHabit>> {
        val (startOfDay, endOfDay) = getTodayTimeRange()

        return combine(
            habitDao.getAllHabits(),
            waterDao.getTodayTotalAmount(startOfDay, endOfDay),
            medicineDao.getAllMedicines(),
            pomodoroDao.getTodayWorkSessionCount(startOfDay, endOfDay)
        ) { habitList, waterTotalAmount, medicinesList, pomodoroCount ->

            val unifiedList = mutableListOf<UnifiedHabit>()

            // --- 1. SU ---
            val waterTarget = 3000
            val currentWater = waterTotalAmount ?: 0
            val waterProgress = (currentWater.toFloat() / waterTarget).coerceIn(0f, 1f)

            unifiedList.add(
                UnifiedHabit(
                    id = "water_main",
                    title = "Water",
                    titleRes = R.string.water_tracker_title,
                    subtitle = "$currentWater / $waterTarget ml",
                    icon = "💧",
                    color = 0xFF2196F3,
                    progress = waterProgress,
                    isCompleted = currentWater >= waterTarget,
                    source = HabitSource.WATER,
                    actionLabel = "+200ml",
                    actionLabelRes = R.string.btn_quick_add_water,

                    // yeni alanlar
                    category = "HEALTH",
                    targetValue = waterTarget,
                    currentValue = currentWater,
                    unit = "ml"
                )
            )

            // --- 2. POMODORO ---
            val currentPomodoro = pomodoroCount ?: 0
            val pomodoroTarget = 4
            val pomodoroProgress = (currentPomodoro.toFloat() / pomodoroTarget).coerceIn(0f, 1f)

            unifiedList.add(
                UnifiedHabit(
                    id = "pomodoro_main",
                    title = "Pomodoro",
                    titleRes = R.string.pomodoro_title,
                    subtitle = if (currentPomodoro > 0) "$currentPomodoro / $pomodoroTarget"
                    else context.getString(R.string.status_focus_time),
                    icon = "🍅",
                    color = 0xFFF44336,
                    progress = pomodoroProgress,
                    isCompleted = currentPomodoro >= pomodoroTarget,
                    source = HabitSource.POMODORO,
                    actionLabel = "Start",
                    actionLabelRes = R.string.pomodoro_start,

                    category = "WORK",
                    targetValue = pomodoroTarget,
                    currentValue = currentPomodoro,
                    unit = "set"
                )
            )

            // --- 3. İLAÇLAR ---
            medicinesList.forEach { medicine ->
                unifiedList.add(
                    UnifiedHabit(
                        id = "medicine_${medicine.id}",
                        title = medicine.name,
                        subtitle = "${medicine.dosage} ${context.getString(R.string.unit_dose)}",
                        icon = "💊",
                        color = 0xFF9C27B0,
                        progress = 0f,
                        isCompleted = false,
                        source = HabitSource.MEDICINE,

                        category = "HEALTH",
                        unit = "dose"
                    )
                )
            }

            // --- 4. NORMAL ALIŞKANLIKLAR ---
            habitList.forEach { habit ->
                val isDone = habit.isCompletedToday()

                unifiedList.add(
                    UnifiedHabit(
                        id = "habit_${habit.id}",
                        title = habit.name,
                        subtitle = if (isDone)
                            context.getString(R.string.status_completed)
                        else
                            context.getString(R.string.status_waiting),
                        icon = habit.icon,
                        color = habit.color,
                        progress = if (isDone) 1f else 0f,
                        isCompleted = isDone,
                        source = HabitSource.HABIT,
                        originalId = habit.id,

                        category = habit.category,
                        targetValue = habit.targetValue,
                        currentValue = if (isDone) habit.targetValue else 0,
                        unit = habit.unit
                    )
                )
            }

            unifiedList.sortedBy { it.isCompleted }
        }
    }

    suspend fun addQuickWater(amount: Int) {
        waterDao.insertEntry(
            WaterEntryEntity(
                id = UUID.randomUUID().toString(),
                amount = amount,
                timestamp = System.currentTimeMillis(),
                drinkType = "water"
            )
        )
    }

    private fun getTodayTimeRange(): Pair<Long, Long> {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        val start = c.timeInMillis

        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        val end = c.timeInMillis

        return start to end
    }

    suspend fun toggleHabitCompletion(habitId: Long, status: Boolean) {
        if (status) habitDao.uncompleteHabit(habitId)
        else habitDao.completeHabit(habitId)
    }
}
