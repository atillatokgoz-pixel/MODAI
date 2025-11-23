package com.example.naifdeneme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * PreferencesManager - DataStore kullanarak uygulama ayarlarını yönetir
 *
 * Tema, dil ve modül ayarları
 * Version: Updated with sound/vibration/priority
 */
class PreferencesManager(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_NAME)

    companion object {
        private const val PREFS_NAME = "modai_preferences"

        // Keys
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        private val KEY_REMINDER_HOUR = intPreferencesKey("reminder_hour")
        private val KEY_REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        private val KEY_LAST_SCREEN = stringPreferencesKey("last_screen")

        // 🔥 SU TAKİBİ KEYS
        private val KEY_WATER_DAILY_TARGET = intPreferencesKey("water_daily_target")
        private val KEY_WATER_REMINDER_ENABLED = booleanPreferencesKey("water_reminder_enabled")
        private val KEY_WATER_REMINDER_START_HOUR = intPreferencesKey("water_reminder_start_hour")
        private val KEY_WATER_REMINDER_END_HOUR = intPreferencesKey("water_reminder_end_hour")
        private val KEY_WATER_REMINDER_FREQUENCY = intPreferencesKey("water_reminder_frequency")

        // 🔥 YENİ EKLENEN KEYS (EKSİK OLANLAR BUNLARDI!)
        private val KEY_WATER_NOTIFICATION_SOUND = booleanPreferencesKey("water_notification_sound")
        private val KEY_WATER_NOTIFICATION_VIBRATION = booleanPreferencesKey("water_notification_vibration")
        private val KEY_WATER_NOTIFICATION_PRIORITY = stringPreferencesKey("water_notification_priority")
        private val KEY_WATER_SEND_IN_DND = booleanPreferencesKey("water_send_in_dnd")

        // Validation Constants
        const val MIN_FREQUENCY_MINUTES = 15
        const val MAX_FREQUENCY_MINUTES = 180
        const val MIN_DAILY_TARGET = 500
        const val MAX_DAILY_TARGET = 10000

        // Singleton instance
        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PreferencesManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    // === KULLANICI AYARLARI ===

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences -> preferences[KEY_USER_NAME] = name }
    }
    val userName: Flow<String> = context.dataStore.data.map { it[KEY_USER_NAME] ?: context.getString(R.string.default_user_name) }
    suspend fun getUserNameImmediate(): String = userName.first()

    // === TEMA AYARLARI ===

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[KEY_IS_DARK_MODE] = enabled }
    }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_DARK_MODE] ?: false }

    // === DİL AYARLARI ===

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences -> preferences[KEY_LANGUAGE] = language }
    }
    val language: Flow<String> = context.dataStore.data.map { it[KEY_LANGUAGE] ?: "tr" }

    // === NAVİGASYON ===

    suspend fun setLastScreen(screen: String) {
        context.dataStore.edit { preferences -> preferences[KEY_LAST_SCREEN] = screen }
    }
    val lastScreen: Flow<String> = context.dataStore.data.map { it[KEY_LAST_SCREEN] ?: "main" }
    suspend fun clearLastScreen() {
        context.dataStore.edit { preferences -> preferences.remove(KEY_LAST_SCREEN) }
    }

    // === DİNAMİK RENK ===

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[KEY_DYNAMIC_COLOR] = enabled }
    }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[KEY_DYNAMIC_COLOR] ?: true }

    // === BİLDİRİM AYARLARI (GENEL) ===

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[KEY_REMINDER_ENABLED] = enabled }
    }
    val isReminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_REMINDER_ENABLED] ?: false }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REMINDER_HOUR] = hour
            preferences[KEY_REMINDER_MINUTE] = minute
        }
    }
    val reminderHour: Flow<Int> = context.dataStore.data.map { it[KEY_REMINDER_HOUR] ?: 9 }
    val reminderMinute: Flow<Int> = context.dataStore.data.map { it[KEY_REMINDER_MINUTE] ?: 0 }

    // === SU TAKİBİ AYARLARI (TAMAMI) ===

    suspend fun setWaterDailyTarget(target: Int) {
        val validTarget = target.coerceIn(MIN_DAILY_TARGET, MAX_DAILY_TARGET)
        context.dataStore.edit { preferences -> preferences[KEY_WATER_DAILY_TARGET] = validTarget }
    }
    val waterDailyTarget: Flow<Int> = context.dataStore.data.map { it[KEY_WATER_DAILY_TARGET] ?: 2500 }

    suspend fun setWaterReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[KEY_WATER_REMINDER_ENABLED] = enabled }
    }
    val waterReminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_WATER_REMINDER_ENABLED] ?: false }

    suspend fun setWaterReminderStartHour(hour: Int) {
        context.dataStore.edit { preferences -> preferences[KEY_WATER_REMINDER_START_HOUR] = hour.coerceIn(0, 23) }
    }
    val waterReminderStartHour: Flow<Int> = context.dataStore.data.map { it[KEY_WATER_REMINDER_START_HOUR] ?: 9 }

    suspend fun setWaterReminderEndHour(hour: Int) {
        context.dataStore.edit { preferences -> preferences[KEY_WATER_REMINDER_END_HOUR] = hour.coerceIn(0, 23) }
    }
    val waterReminderEndHour: Flow<Int> = context.dataStore.data.map { it[KEY_WATER_REMINDER_END_HOUR] ?: 22 }

    suspend fun setWaterReminderFrequency(minutes: Int) {
        context.dataStore.edit { preferences -> preferences[KEY_WATER_REMINDER_FREQUENCY] = minutes.coerceIn(MIN_FREQUENCY_MINUTES, MAX_FREQUENCY_MINUTES) }
    }
    val waterReminderFrequency: Flow<Int> = context.dataStore.data.map { it[KEY_WATER_REMINDER_FREQUENCY] ?: 60 }

    // 🔥 YENİ EKLENEN FONKSİYONLAR (KIRMIZI HATALARI ÇÖZECEK)

    /**
     * Bildirim sesi aktif/pasif
     */
    suspend fun setWaterNotificationSound(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[KEY_WATER_NOTIFICATION_SOUND] = enabled }
    }
    val waterNotificationSound: Flow<Boolean> = context.dataStore.data.map { it[KEY_WATER_NOTIFICATION_SOUND] ?: true }

    /**
     * Bildirim titreşimi aktif/pasif
     */
    suspend fun setWaterNotificationVibration(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[KEY_WATER_NOTIFICATION_VIBRATION] = enabled }
    }
    val waterNotificationVibration: Flow<Boolean> = context.dataStore.data.map { it[KEY_WATER_NOTIFICATION_VIBRATION] ?: true }

    /**
     * Bildirim önceliği
     */
    suspend fun setWaterNotificationPriority(priority: String) {
        context.dataStore.edit { preferences -> preferences[KEY_WATER_NOTIFICATION_PRIORITY] = priority }
    }
    val waterNotificationPriority: Flow<String> = context.dataStore.data.map { it[KEY_WATER_NOTIFICATION_PRIORITY] ?: "DEFAULT" }

    /**
     * DND modunda bildirim gönderimi
     */
    suspend fun setWaterSendInDND(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[KEY_WATER_SEND_IN_DND] = enabled }
    }
    val waterSendInDND: Flow<Boolean> = context.dataStore.data.map { it[KEY_WATER_SEND_IN_DND] ?: false }

    // === ONBOARDING ===

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences -> preferences[KEY_ONBOARDING_COMPLETED] = completed }
    }
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_COMPLETED] ?: false }

    // === DİĞER METODLAR ===

    suspend fun clearAll() {
        context.dataStore.edit { preferences -> preferences.clear() }
    }

    suspend fun clearWaterSettings() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_WATER_DAILY_TARGET)
            preferences.remove(KEY_WATER_REMINDER_ENABLED)
            preferences.remove(KEY_WATER_REMINDER_START_HOUR)
            preferences.remove(KEY_WATER_REMINDER_END_HOUR)
            preferences.remove(KEY_WATER_REMINDER_FREQUENCY)
            preferences.remove(KEY_WATER_NOTIFICATION_SOUND)
            preferences.remove(KEY_WATER_NOTIFICATION_VIBRATION)
            preferences.remove(KEY_WATER_NOTIFICATION_PRIORITY)
            preferences.remove(KEY_WATER_SEND_IN_DND)
        }
    }

    // === BACKWARD COMPATIBILITY ===

    suspend fun getReminderHourImmediate(): Int = reminderHour.first()
    suspend fun getReminderMinuteImmediate(): Int = reminderMinute.first()
    suspend fun isReminderEnabledImmediate(): Boolean = isReminderEnabled.first()
}