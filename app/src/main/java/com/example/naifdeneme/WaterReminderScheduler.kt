package com.example.naifdeneme

import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Su Hatırlatıcı Zamanlayıcı
 * WorkManager ile periyodik bildirimler
 * Version 2: runBlocking kaldırıldı, suspend function oldu
 */
object WaterReminderScheduler {

    private const val WORK_NAME = "water_reminder_work"
    private const val TAG = "WaterReminderScheduler"
    private const val TAG_WATER = "water"
    private const val TAG_REMINDER = "reminder"

    // Validation Constants
    private const val MIN_FREQUENCY_MINUTES = 15
    private const val MAX_FREQUENCY_MINUTES = 180 // 3 saat

    /**
     * Hatırlatıcıları planla (suspend function)
     */
    suspend fun scheduleReminders(context: Context) {
        try {
            Log.d(TAG, "scheduleReminders called")

            val settings = getSettings(context)
            Log.d(TAG, "Settings: enabled=${settings.enabled}, frequency=${settings.frequency}")

            if (!settings.enabled) {
                Log.i(TAG, "Reminders disabled, cancelling")
                cancelReminders(context)
                return
            }

            // Permission kontrolü
            if (!checkNotificationPermission(context)) {
                Log.w(TAG, "Notification permission not granted")
                return
            }

            // Validation
            val validatedSettings = settings.validate()
            if (validatedSettings != settings) {
                Log.w(TAG, "Settings were invalid, corrected to: $validatedSettings")
            }

            // Work'u kuyruğa al
            enqueueWork(context, validatedSettings)

            Log.i(TAG, "Reminders scheduled successfully: $validatedSettings")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule reminders", e)
        }
    }

    /**
     * Ayarları al (suspend)
     */
    private suspend fun getSettings(context: Context): ReminderSettings {
        val prefsManager = PreferencesManager.getInstance(context)
        return ReminderSettings(
            enabled = prefsManager.waterReminderEnabled.first(),
            frequency = prefsManager.waterReminderFrequency.first(),
            startHour = prefsManager.waterReminderStartHour.first(),
            endHour = prefsManager.waterReminderEndHour.first()
        )
    }

    /**
     * Work'u kuyruğa al
     */
    private fun enqueueWork(context: Context, settings: ReminderSettings) {
        // Flex interval, repeat interval'ın %25'i olmalı (min 15 dk)
        val flexInterval = (settings.frequency * 0.25).toLong()
            .coerceAtLeast(MIN_FREQUENCY_MINUTES.toLong())

        val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            repeatInterval = settings.frequency.toLong(),
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = flexInterval,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .setRequiresDeviceIdle(false)
                    .build()
            )
            .setInputData(
                workDataOf(
                    "startHour" to settings.startHour,
                    "endHour" to settings.endHour,
                    "frequency" to settings.frequency
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(WORK_NAME)
            .addTag(TAG_WATER)
            .addTag(TAG_REMINDER)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )

        Log.d(TAG, "Work enqueued: frequency=${settings.frequency}min, flex=${flexInterval}min")
    }

    /**
     * Hatırlatıcıları iptal et
     */
    fun cancelReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        Log.i(TAG, "Reminders cancelled")
    }

    /**
     * Test bildirimi (5 saniye sonra)
     */
    fun scheduleTestReminder(context: Context) {
        val testWork = OneTimeWorkRequestBuilder<WaterReminderWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS) // 5 saniye (daha hızlı test)
            .setInputData(
                workDataOf(
                    "isTest" to true
                )
            )
            .addTag("test_reminder")
            .build()

        WorkManager.getInstance(context).enqueue(testWork)
        Log.d(TAG, "Test reminder scheduled (5 seconds)")
    }

    /**
     * Hatırlatıcı aktif mi?
     */
    fun isReminderActive(context: Context): Boolean {
        return try {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(WORK_NAME)
                .get()

            workInfos.any {
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check reminder status", e)
            false
        }
    }

    /**
     * Notification permission kontrolü
     */
    private fun checkNotificationPermission(context: Context): Boolean {
        return NotificationHelper.canShowNotification(context)
    }

    /**
     * Hatırlatıcı ayarları
     */
    private data class ReminderSettings(
        val enabled: Boolean,
        val frequency: Int,
        val startHour: Int,
        val endHour: Int
    ) {
        /**
         * Ayarları validate et ve düzelt
         */
        fun validate(): ReminderSettings {
            val validFrequency = frequency.coerceIn(MIN_FREQUENCY_MINUTES, MAX_FREQUENCY_MINUTES)
            val validStartHour = startHour.coerceIn(0, 23)
            var validEndHour = endHour.coerceIn(0, 23)

            // End hour, start hour'dan küçük olamaz
            if (validEndHour <= validStartHour) {
                validEndHour = 22 // Default 22:00
                Log.w(TAG, "End hour ($endHour) <= start hour ($startHour), set to 22")
            }

            return ReminderSettings(
                enabled = enabled,
                frequency = validFrequency,
                startHour = validStartHour,
                endHour = validEndHour
            )
        }

        /**
         * Belirtilen saat aktif saatler içinde mi?
         */
        fun isInActiveHours(hour: Int): Boolean {
            return hour in startHour until endHour
        }
    }
}