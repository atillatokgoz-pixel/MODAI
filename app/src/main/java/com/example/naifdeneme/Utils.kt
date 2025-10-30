package com.example.naifdeneme

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Yardımcı fonksiyonlar
 */

/**
 * Para formatı (₺1.234,56)
 */
fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    return formatter.format(amount)
}

/**
 * Tarih formatı (5 Oca 2024)
 */
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("d MMM yyyy", Locale("tr"))
    return sdf.format(Date(timestamp))
}

/**
 * Ay başlangıcını al (timestamp)
 */
fun getMonthStart(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

/**
 * Ay sonunu al (timestamp)
 */
fun getMonthEnd(): Long {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, 1)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}