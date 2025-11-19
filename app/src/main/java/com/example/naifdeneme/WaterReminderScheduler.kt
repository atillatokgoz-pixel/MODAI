package com.example.naifdeneme

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

/**
 * Su Hatırlatıcı Zamanlayıcı
 * WorkManager ile periyodik bildirimler
 */
object WaterReminderScheduler {

    private const val WORK_NAME = "water_reminder_work"

    /**
     * Hatırlatıcıları planla
     */
    fun scheduleReminders(context: Context) {
        val prefsManager = PreferencesManager.getInstance(context)

        // Ayarları al
        val settings = runBlocking {
            val enabled = prefsManager.waterReminderEnabled.first()
            val frequency = prefsManager.waterReminderFrequency.first()
            val startHour = prefsManager.waterReminderStartHour.first()
            val endHour = prefsManager.waterReminderEndHour.first()

            ReminderSettings(enabled, frequency, startHour, endHour)
        }

        if (!settings.enabled) {
            cancelReminders(context)
            return
        }

        // WorkManager ile periyodik çalışma
        val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            repeatInterval = settings.frequency.toLong(),
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 15, // 15 dakika esneklik
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false) // Düşük bataryada da çalış
                    .build()
            )
            .setInputData(
                workDataOf(
                    "startHour" to settings.startHour,
                    "endHour" to settings.endHour
                )
            )
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Hatırlatıcıları iptal et
     */
    fun cancelReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Test bildirimi (1 dakika sonra)
     */
    fun scheduleTestReminder(context: Context) {
        val testWork = OneTimeWorkRequestBuilder<WaterReminderWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .setInputData(
                workDataOf(
                    "isTest" to true
                )
            )
            .build()

        WorkManager.getInstance(context).enqueue(testWork)
    }
}

/**
 * Hatırlatıcı ayarları
 */
private data class ReminderSettings(
    val enabled: Boolean,
    val frequency: Int,
    val startHour: Int,
    val endHour: Int
)