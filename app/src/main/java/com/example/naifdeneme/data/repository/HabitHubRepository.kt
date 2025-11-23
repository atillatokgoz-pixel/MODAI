package com.example.naifdeneme.data.repository

import android.content.Context
import android.graphics.Color
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
        ) { habits, waterTotalAmount, medicines, pomodoroCount ->

            val unifiedList = mutableListOf<UnifiedHabit>()

            // --- 1. SU MODÜLÜ ---
            val waterTarget = 3000
            val currentWater = waterTotalAmount ?: 0
            val waterProgress = (currentWater.toFloat() / waterTarget).coerceIn(0f, 1f)

            unifiedList.add(
                UnifiedHabit(
                    id = "water_main",
                    title = "Water", // Yedek (Görünmeyecek)
                    titleRes = R.string.water_tracker_title, // 🔥 ID GÖNDERİYORUZ
                    subtitle = "$currentWater / $waterTarget ml",
                    icon = "💧",
                    color = 0xFF2196F3,
                    progress = waterProgress,
                    isCompleted = currentWater >= waterTarget,
                    source = HabitSource.WATER,
                    actionLabel = "+200ml",
                    actionLabelRes = R.string.btn_quick_add_water // 🔥 ID GÖNDERİYORUZ
                )
            )

            // --- 2. POMODORO ---
            val currentPomodoro = pomodoroCount ?: 0
            val pomodoroTarget = 4
            val pomodoroProgress = (currentPomodoro.toFloat() / pomodoroTarget).coerceIn(0f, 1f)

            // Alt metin dinamik olduğu için context kullanmak zorundayız ama title sabit
            val pomodoroSubtitle = if(currentPomodoro > 0) "$currentPomodoro / $pomodoroTarget"
            else context.getString(R.string.status_focus_time)

            unifiedList.add(
                UnifiedHabit(
                    id = "pomodoro_main",
                    title = "Pomodoro",
                    titleRes = R.string.pomodoro_title, // 🔥 ID GÖNDERİYORUZ
                    subtitle = pomodoroSubtitle,
                    icon = "🍅",
                    color = 0xFFF44336,
                    progress = pomodoroProgress,
                    isCompleted = currentPomodoro >= pomodoroTarget,
                    source = HabitSource.POMODORO,
                    actionLabel = "Start",
                    actionLabelRes = R.string.pomodoro_start // 🔥 ID GÖNDERİYORUZ
                )
            )

            // --- 3. İLAÇLAR ---
            // İlaç isimleri veritabanından gelir (Kullanıcı yazar), o yüzden çevrilemez.
            medicines.forEach { medicine ->
                unifiedList.add(
                    UnifiedHabit(
                        id = "medicine_${medicine.id}",
                        title = medicine.name, // Kullanıcı girdisi
                        subtitle = "${medicine.dosage} ${context.getString(R.string.unit_dose)}",
                        icon = "💊",
                        color = 0xFF9C27B0,
                        progress = 0f,
                        isCompleted = false,
                        source = HabitSource.MEDICINE,
                        originalId = null
                    )
                )
            }

            // --- 4. GENEL ALIŞKANLIKLAR ---
            // Alışkanlık isimleri veritabanından gelir.
            habits.forEach { habit ->
                val isDone = habit.isCompletedToday()
                // Durum mesajını dinamik alıyoruz
                val statusMsg = if (isDone) context.getString(R.string.status_completed)
                else context.getString(R.string.status_waiting)

                unifiedList.add(
                    UnifiedHabit(
                        id = "habit_${habit.id}",
                        title = habit.name,
                        subtitle = statusMsg,
                        icon = habit.icon,
                        color = parseColorSafe(habit.color),
                        progress = if (isDone) 1f else 0f,
                        isCompleted = isDone,
                        source = HabitSource.HABIT,
                        originalId = habit.id
                    )
                )
            }

            unifiedList.sortedBy { it.isCompleted }
        }
    }

    suspend fun addQuickWater(amount: Int) {
        val newEntry = WaterEntryEntity(
            id = UUID.randomUUID().toString(),
            amount = amount,
            timestamp = System.currentTimeMillis(),
            drinkType = "water"
        )
        waterDao.insertEntry(newEntry)
    }

    private fun getTodayTimeRange(): Pair<Long, Long> {
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

        return Pair(startOfDay, endOfDay)
    }

    private fun parseColorSafe(colorString: String): Long {
        return try {
            Color.parseColor(colorString).toLong()
        } catch (e: Exception) {
            0xFFFFA726
        }
    }

    /**
     * Alışkanlık tamamlandıysa geri al, tamamlanmadıysa tamamla.
     */
    suspend fun toggleHabitCompletion(habitId: Long, isCurrentlyCompleted: Boolean) {
        if (isCurrentlyCompleted) {
            habitDao.uncompleteHabit(habitId)
        } else {
            habitDao.completeHabit(habitId)
        }
    }
}