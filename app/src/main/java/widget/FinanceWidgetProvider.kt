package com.example.naifdeneme.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.naifdeneme.R
import com.example.naifdeneme.database.AppDatabase
import com.example.naifdeneme.database.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * FinanceWidgetProvider - Finans Widget'ı
 *
 * Bakiye, gelir ve gider bilgilerini gösterir
 */
class FinanceWidgetProvider : AppWidgetProvider() {

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
            val views = RemoteViews(context.packageName, R.layout.finance_widget)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val transactions = database.transactionDao().getAllTransactionsForWidget()

                    // Hesaplamalar
                    val totalIncome = transactions
                        .filter { it.type == TransactionType.INCOME }
                        .sumOf { it.amount }

                    val totalExpense = transactions
                        .filter { it.type == TransactionType.EXPENSE }
                        .sumOf { it.amount }

                    val balance = totalIncome - totalExpense

                    CoroutineScope(Dispatchers.Main).launch {
                        // Para formatı
                        val formatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))

                        // Bakiye
                        views.setTextViewText(
                            R.id.widget_finance_balance,
                            formatter.format(balance)
                        )

                        // Gelir
                        views.setTextViewText(
                            R.id.widget_finance_income,
                            "Gelir: ${formatter.format(totalIncome)}"
                        )

                        // Gider
                        views.setTextViewText(
                            R.id.widget_finance_expense,
                            "Gider: ${formatter.format(totalExpense)}"
                        )

                        // Widget'a tıklayınca uygulamayı aç
                        val intent = Intent(context, com.example.naifdeneme.MainActivity::class.java)
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            2,  // Farklı requestCode
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_finance_title, pendingIntent)

                        // Yenile butonu
                        val refreshIntent = Intent(context, FinanceWidgetProvider::class.java).apply {
                            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                        }
                        val refreshPendingIntent = PendingIntent.getBroadcast(
                            context,
                            2,  // Farklı requestCode
                            refreshIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_finance_refresh_button, refreshPendingIntent)

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    // Hata durumunda
                    views.setTextViewText(R.id.widget_finance_balance, "₺0,00")
                    views.setTextViewText(R.id.widget_finance_income, "Gelir: ₺0")
                    views.setTextViewText(R.id.widget_finance_expense, "Gider: ₺0")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}