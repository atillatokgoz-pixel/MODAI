package com.example.naifdeneme

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.example.naifdeneme.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationHelper {
    private const val TAG = "NotificationHelper"
    private const val CHANNEL_ID = "habit_reminder"
    private const val TEST_WORK_NAME = "test_reminder"

    private fun getWorkName(habitId: Long) = "habit_reminder_$habitId"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alışkanlık Hatırlatmaları",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Günlük alışkanlık hatırlatmaları"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }

    fun scheduleTestReminder(context: Context) {
        Log.d(TAG, "Test reminder scheduled for 1 minute")

        val testRequest = OneTimeWorkRequestBuilder<TestReminderWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .addTag(TEST_WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            TEST_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            testRequest
        )
    }

    fun scheduleHabitReminder(
        context: Context,
        habitId: Long,
        hour: Int,
        minute: Int,
        reminderDays: String
    ) {
        Log.d(TAG, "Scheduling reminder for habit $habitId at $hour:$minute on days: $reminderDays")

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.DAY_OF_YEAR, 1)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        Log.d(TAG, "Time until first notification: ${timeDiff / 1000 / 60} minutes")

        val inputData = workDataOf(
            "habitId" to habitId,
            "reminderDays" to reminderDays
        )

        val dailyWorkRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(getWorkName(habitId))
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            getWorkName(habitId),
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest
        )

        Log.d(TAG, "Habit reminder work enqueued for habit $habitId")
    }

    fun cancelHabitReminder(context: Context, habitId: Long) {
        Log.d(TAG, "Cancelling reminder for habit $habitId")
        WorkManager.getInstance(context).cancelUniqueWork(getWorkName(habitId))
    }

    fun cancelAllReminders(context: Context) {
        Log.d(TAG, "Cancelling all reminders")
        WorkManager.getInstance(context).cancelAllWork()
    }

    fun showHabitNotification(context: Context, habitId: Long, habitName: String, habitIcon: String) {
        Log.d(TAG, "Showing notification for habit: $habitName")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("habitId", habitId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val completeIntent = Intent(context, HabitCompleteReceiver::class.java).apply {
            putExtra("habitId", habitId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt() + 10000,
            completeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$habitIcon $habitName")
            .setContentText("Zamanı geldi! Tamamlamak için dokun.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .addAction(
                android.R.drawable.ic_menu_add,
                "Tamamla",
                completePendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(habitId.toInt(), notification)
            Log.d(TAG, "Notification sent successfully for habit $habitId")
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission denied", e)
        }
    }

    fun showTestNotification(context: Context) {
        Log.d(TAG, "Showing test notification")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🧪 Test Bildirimi")
            .setContentText("Bildirim sistemi çalışıyor!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(9999, notification)
            Log.d(TAG, "Test notification sent successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "Test notification permission denied", e)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true
        }
    }
}

class HabitReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("HabitReminderWorker", "Worker started")

            val habitId = inputData.getLong("habitId", 0L)
            val reminderDays = inputData.getString("reminderDays") ?: "1,2,3,4,5,6,7"

            Log.d("HabitReminderWorker", "Habit ID: $habitId, Days: $reminderDays")

            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val days = reminderDays.split(",").mapNotNull { it.toIntOrNull() }

            if (!days.contains(today)) {
                Log.d("HabitReminderWorker", "Today ($today) is not in reminder days")
                return@withContext Result.success()
            }

            val database = AppDatabase.getDatabase(applicationContext)
            val habit = database.habitDao().getHabitById(habitId)

            if (habit == null) {
                Log.e("HabitReminderWorker", "Habit not found: $habitId")
                return@withContext Result.failure()
            }

            if (habit.isCompletedToday()) {
                Log.d("HabitReminderWorker", "Habit already completed today")
                return@withContext Result.success()
            }

            Log.d("HabitReminderWorker", "Showing notification for: ${habit.name}")

            NotificationHelper.showHabitNotification(
                applicationContext,
                habit.id,
                habit.name,
                habit.icon
            )

            Log.d("HabitReminderWorker", "Worker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("HabitReminderWorker", "Worker failed", e)
            Result.failure()
        }
    }
}

class TestReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("TestReminderWorker", "Test worker started")
            NotificationHelper.showTestNotification(applicationContext)
            Log.d("TestReminderWorker", "Test worker completed")
            Result.success()
        } catch (e: Exception) {
            Log.e("TestReminderWorker", "Test worker failed", e)
            Result.failure()
        }
    }
}

class HabitCompleteReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra("habitId", 0L)
        Log.d("HabitCompleteReceiver", "Complete action for habit: $habitId")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                database.habitDao().completeHabit(habitId)
                Log.d("HabitCompleteReceiver", "Habit completed successfully")

                NotificationManagerCompat.from(context).cancel(habitId.toInt())
            } catch (e: Exception) {
                Log.e("HabitCompleteReceiver", "Failed to complete habit", e)
            }
        }
    }
}