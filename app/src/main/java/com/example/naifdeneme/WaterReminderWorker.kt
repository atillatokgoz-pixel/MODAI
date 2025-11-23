package com.example.naifdeneme

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.naifdeneme.database.AppDatabase
import kotlinx.coroutines.flow.first
import java.util.*

/**
 * Su Hatırlatıcı Worker
 * Bildirim gönderir (String resources kullanır)
 */
class WaterReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "WaterReminderWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            android.util.Log.d(TAG, "Worker started, attempt: $runAttemptCount")

            // Permission kontrolü
            if (!hasNotificationPermission()) {
                android.util.Log.w(TAG, "Notification permission not granted")
                return Result.success()
            }

            // Test modu kontrolü
            val isTest = inputData.getBoolean("isTest", false)
            if (isTest) {
                sendTestNotification()
                return Result.success()
            }

            // Aktiflik kontrolü
            if (!isReminderActive()) {
                android.util.Log.d(TAG, "Reminders disabled")
                return Result.success()
            }

            // Saat kontrolü
            if (!isInActiveHours()) {
                android.util.Log.d(TAG, "Outside active hours")
                return Result.success()
            }

            // DND kontrolü (kullanıcı tercihi varsa)
            if (!shouldSendInDND()) {
                android.util.Log.d(TAG, "Do not disturb mode active")
                return Result.success()
            }

            // Progress bilgisini al
            val waterProgress = getWaterProgress()

            // Bildirim gönder
            sendReminder(waterProgress)

            android.util.Log.i(TAG, "Reminder sent successfully: ${waterProgress.percentage}%")
            Result.success()

        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error in doWork", e)

            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private suspend fun isReminderActive(): Boolean {
        val prefsManager = PreferencesManager.getInstance(context)
        return prefsManager.waterReminderEnabled.first()
    }

    private fun isInActiveHours(): Boolean {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val startHour = inputData.getInt("startHour", 9)
        val endHour = inputData.getInt("endHour", 22)

        return currentHour in startHour until endHour
    }

    private fun shouldSendInDND(): Boolean {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val currentFilter = notificationManager.currentInterruptionFilter
            return currentFilter == NotificationManager.INTERRUPTION_FILTER_ALL
        }

        return true
    }

    private suspend fun getWaterProgress(): WaterProgress {
        val database = AppDatabase.getDatabase(context)
        val prefsManager = PreferencesManager.getInstance(context)

        val (startOfDay, endOfDay) = getTodayTimestamps()
        val todayTotal = database.waterDao().getTodayTotalAmount(startOfDay, endOfDay).first() ?: 0
        val target = prefsManager.waterDailyTarget.first()
        val percentage = if (target > 0) (todayTotal * 100 / target) else 0

        return WaterProgress(todayTotal, target, percentage)
    }

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

    private fun sendTestNotification() {
        val title = context.getString(R.string.notification_test_title)
        val message = context.getString(R.string.notification_test_message)
        sendNotification(title, message, 0, 100, isTest = true)
    }

    private fun sendReminder(progress: WaterProgress) {
        val (title, message) = createNotificationMessage(progress)
        sendNotification(title, message, progress.current, progress.target, isTest = false)
    }

    private fun createNotificationMessage(progress: WaterProgress): Pair<String, String> {
        return when {
            progress.percentage >= 100 -> {
                context.getString(R.string.notification_goal_reached_title) to
                        context.getString(R.string.notification_goal_reached_message, progress.current)
            }
            progress.percentage >= 75 -> {
                context.getString(R.string.notification_almost_there_title) to
                        context.getString(R.string.notification_almost_there_message, progress.target - progress.current)
            }
            progress.percentage >= 50 -> {
                context.getString(R.string.notification_halfway_title) to
                        context.getString(R.string.notification_halfway_message, progress.percentage)
            }
            progress.percentage >= 25 -> {
                context.getString(R.string.notification_quarter_title) to
                        context.getString(R.string.notification_quarter_message, progress.current, progress.target)
            }
            else -> {
                context.getString(R.string.notification_start_title) to
                        context.getString(R.string.notification_start_message)
            }
        }
    }

    private fun sendNotification(
        title: String,
        message: String,
        current: Int,
        target: Int,
        isTest: Boolean
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "water")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Quick add actions (test modu değilse)
        val actions = if (!isTest) {
            listOf(
                createQuickAddAction(250),
                createQuickAddAction(500)
            )
        } else {
            emptyList()
        }

        val percentage = if (target > 0) (current * 100 / target).coerceIn(0, 100) else 0
        val notificationId = NotificationHelper.generateNotificationId()

        val notificationBuilder = NotificationCompat.Builder(context, NotificationHelper.WATER_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.neon_cyan))
            .setGroup("water_reminders")

        // Progress bar ekle (test değilse)
        if (!isTest && target > 0) {
            notificationBuilder
                .setProgress(100, percentage, false)
                .setSubText("$percentage%")
        }

        // Actions ekle
        actions.forEach { action ->
            notificationBuilder.addAction(action)
        }

        val notification = notificationBuilder.build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notification)
    }

    private fun createQuickAddAction(amount: Int): NotificationCompat.Action {
        val intent = Intent(context, QuickAddWaterReceiver::class.java).apply {
            action = QuickAddWaterReceiver.ACTION_ADD_WATER
            putExtra(QuickAddWaterReceiver.EXTRA_AMOUNT, amount)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            amount, // Unique request code
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_water_drop,
            "${amount}ml",
            pendingIntent
        ).build()
    }

    private data class WaterProgress(
        val current: Int,
        val target: Int,
        val percentage: Int
    )
}