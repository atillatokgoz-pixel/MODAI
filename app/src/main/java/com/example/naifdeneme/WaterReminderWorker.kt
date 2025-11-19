package com.example.naifdeneme

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.util.*

/**
 * Su Hatırlatıcı Worker
 * Bildirim gönderir
 */
class WaterReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "water_reminder_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        // Test modu kontrolü
        val isTest = inputData.getBoolean("isTest", false)

        if (isTest) {
            // Test bildirimi gönder
            sendNotification("🧪 Test Bildirimi", "Bildirimler çalışıyor! ✅")
            return Result.success()
        }

        // Saat kontrolü
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val startHour = inputData.getInt("startHour", 9)
        val endHour = inputData.getInt("endHour", 22)

        // Belirlenen saat aralığında mı?
        if (currentHour !in startHour..endHour) {
            return Result.success() // Sessizce başarılı say
        }

        // Hatırlatıcı aktif mi kontrol et
        val prefsManager = PreferencesManager.getInstance(context)
        val isEnabled = prefsManager.waterReminderEnabled.first()

        if (!isEnabled) {
            return Result.success()
        }

        // Bugünkü ilerlemeyi al
        val database = com.example.naifdeneme.database.AppDatabase.getDatabase(context)
        val waterDao = database.waterDao()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis

        val todayTotal = waterDao.getTodayTotalAmount(startOfDay, endOfDay).first() ?: 0
        val target = prefsManager.waterDailyTarget.first()

        // Bildirim mesajını oluştur
        val (title, message) = createNotificationMessage(todayTotal, target)

        // Bildirimi gönder
        sendNotification(title, message)

        return Result.success()
    }

    /**
     * Bildirim mesajı oluştur
     */
    private fun createNotificationMessage(current: Int, target: Int): Pair<String, String> {
        val percentage = if (target > 0) (current * 100 / target) else 0

        return when {
            percentage >= 100 -> {
                "🎉 Günlük Hedef Tamamlandı!" to "Harika! ${current}ml su içtiniz!"
            }
            percentage >= 75 -> {
                "💪 Az Kaldı!" to "${target - current}ml daha içerek hedefi tamamlayın!"
            }
            percentage >= 50 -> {
                "💧 Su İçme Zamanı!" to "Hedefin %${percentage}'ine ulaştınız. Devam edin!"
            }
            percentage >= 25 -> {
                "🚰 Su İçmeyi Unutmayın!" to "Bugün ${current}ml içtiniz. Hedefiniz: ${target}ml"
            }
            else -> {
                "💦 Hidrasyonunuzu İhmal Etmeyin!" to "Hemen bir bardak su için! 🥤"
            }
        }
    }

    /**
     * Bildirim gönder
     */
    private fun sendNotification(title: String, message: String) {
        createNotificationChannel()

        // Uygulama açılma intent'i
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

        // Bildirim oluştur
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0x06F9F9) // Neon cyan
            .build()

        // Bildirim gönder
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Bildirim kanalı oluştur
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Su Hatırlatıcıları"
            val descriptionText = "Düzenli su içme hatırlatmaları"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}