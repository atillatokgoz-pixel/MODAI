package com.example.naifdeneme

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.naifdeneme.database.AppDatabase
import com.example.naifdeneme.database.WaterEntryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Quick Add Water Receiver
 * Notification'dan direkt su eklemek için BroadcastReceiver
 */
class QuickAddWaterReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ADD_WATER = "com.example.naifdeneme.ACTION_ADD_WATER"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        private const val TAG = "QuickAddWaterReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_ADD_WATER) return

        val amount = intent.getIntExtra(EXTRA_AMOUNT, 0)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        if (amount <= 0) {
            Log.w(TAG, "Invalid amount: $amount")
            return
        }

        Log.d(TAG, "Quick add water: $amount ml")

        // Background'da su ekle
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val entry = WaterEntryEntity(
                    amount = amount,
                    drinkType = "water",
                    timestamp = System.currentTimeMillis()
                )

                database.waterDao().insertEntry(entry)

                Log.d(TAG, "Water entry added successfully: $amount ml")

                // UI thread'de success notification göster
                withContext(Dispatchers.Main) {
                    showSuccessNotification(context, amount)

                    // Toast göster
                    Toast.makeText(
                        context,
                        context.getString(R.string.water_added_success, amount),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Original notification'ı kapat (opsiyonel)
                if (notificationId != -1) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(notificationId)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to add water entry", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Hata: Su eklenemedi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Başarı bildirimi göster
     */
    private fun showSuccessNotification(context: Context, amount: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, NotificationHelper.WATER_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("✅ ${context.getString(R.string.water_added_success, amount)}")
            .setContentText("Günlük hedefinize doğru ilerliyorsunuz!")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(3000) // 3 saniye sonra otomatik kaybolur
            .build()

        notificationManager.notify(NotificationHelper.generateNotificationId(), notification)
    }
}