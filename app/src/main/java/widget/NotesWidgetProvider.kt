package com.example.naifdeneme.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.naifdeneme.R
import com.example.naifdeneme.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.PendingIntent
import android.content.Intent

/**
 * NotesWidgetProvider - Notlar Widget'ı
 *
 * Ana ekranda not sayısını ve son notu gösterir
 */
class NotesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.notes_widget)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val notes = database.notesDao().getAllNotesForWidget()
                    val notesCount = notes.size
                    val latestNote = notes.firstOrNull()

                    CoroutineScope(Dispatchers.Main).launch {
                        // Not sayısını göster
                        views.setTextViewText(R.id.widget_notes_count, notesCount.toString())

                        // Son notu göster
                        val latestText = if (latestNote != null) {
                            "Son: ${latestNote.title}"
                        } else {
                            "Henüz not yok"
                        }
                        views.setTextViewText(R.id.widget_notes_latest, latestText)

                        // Widget'a tıklayınca uygulamayı aç
                        val intent = Intent(context, com.example.naifdeneme.MainActivity::class.java)
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            1,  // Farklı requestCode (habit widget 0 kullanıyor)
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_notes_title, pendingIntent)

                        // Yenile butonuna tıklayınca güncelle
                        val refreshIntent = Intent(context, NotesWidgetProvider::class.java).apply {
                            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                        }
                        val refreshPendingIntent = PendingIntent.getBroadcast(
                            context,
                            1,  // Farklı requestCode
                            refreshIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_notes_refresh_button, refreshPendingIntent)

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    views.setTextViewText(R.id.widget_notes_count, "0")
                    views.setTextViewText(R.id.widget_notes_latest, "Hata")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}