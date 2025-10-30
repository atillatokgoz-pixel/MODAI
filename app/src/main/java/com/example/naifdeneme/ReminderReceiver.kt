package com.example.naifdeneme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * Alarm veya sistem olaylarını yakalayarak hatırlatma işini tetikler.
 * (Bazı cihazlarda WorkManager yeniden planlanır.)
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val habitId = intent.getLongExtra("habitId", -1)
            val reminderDays = intent.getStringExtra("reminderDays") ?: "1,2,3,4,5,6,7"

            if (habitId == -1L) {
                Log.e("ReminderReceiver", "Invalid habit ID")
                return
            }

            Log.d("ReminderReceiver", "Trigger received for habit $habitId")

            // WorkManager üzerinden bildirimi yeniden planla
            val data = workDataOf(
                "habitId" to habitId,
                "reminderDays" to reminderDays
            )

            val workRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
                .setInitialDelay(1, TimeUnit.SECONDS)
                .setInputData(data)
                .addTag("reminder_restart_$habitId")
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)

        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Failed to trigger reminder", e)
        }
    }
}
