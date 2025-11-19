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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

/**
 * PreferencesManager - DataStore kullanarak uygulama ayarlarını yönetir
 *
 * Tema, dil ve modül ayarları
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

    /**
     * Kullanıcı adını kaydet
     */
    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
        }
    }

    /**
     * Kullanıcı adını al - Flow
     */
    val userName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_USER_NAME] ?: context.getString(R.string.default_user_name)
        }

    /**
     * Kullanıcı adını hemen al - Blocking (eski kodlarla uyumluluk için)
     */
    suspend fun getUserNameImmediate(): String {
        return userName.first()
    }

    // === TEMA AYARLARI ===

    /**
     * Koyu tema ayarını kaydet
     */
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_DARK_MODE] = enabled
        }
    }

    /**
     * Koyu tema durumu - Flow
     */
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_IS_DARK_MODE] ?: false
        }

    // === DİL AYARLARI ===

    /**
     * Dil ayarını kaydet ("tr", "en")
     */
    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }

    /**
     * Mevcut dil - Flow
     */
    val language: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_LANGUAGE] ?: "tr" // Varsayılan Türkçe
        }

    // === NAVİGASYON (Son Ekran) ===

    /**
     * Son ziyaret edilen ekranı kaydet (recreate sonrası geri dönmek için)
     */
    suspend fun setLastScreen(screen: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_SCREEN] = screen
        }
    }

    /**
     * Son ekranı al - Flow
     */
    val lastScreen: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_LAST_SCREEN] ?: "main"
        }

    /**
     * Son ekran kaydını temizle
     */
    suspend fun clearLastScreen() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_LAST_SCREEN)
        }
    }

    // === DİNAMİK RENK ===

    /**
     * Dynamic color (Material You) ayarı
     */
    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DYNAMIC_COLOR] = enabled
        }
    }

    val dynamicColor: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_DYNAMIC_COLOR] ?: true // Varsayılan açık
        }

    // === BİLDİRİM AYARLARI ===

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REMINDER_ENABLED] = enabled
        }
    }

    val isReminderEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_REMINDER_ENABLED] ?: false
        }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REMINDER_HOUR] = hour
            preferences[KEY_REMINDER_MINUTE] = minute
        }
    }

    val reminderHour: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_REMINDER_HOUR] ?: 9 // Varsayılan 09:00
        }

    val reminderMinute: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_REMINDER_MINUTE] ?: 0
        }

    // === SU TAKİBİ AYARLARI === 🔥 YENİ

    /**
     * Günlük su hedefi (ml)
     */
    suspend fun setWaterDailyTarget(target: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WATER_DAILY_TARGET] = target
        }
    }

    val waterDailyTarget: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_WATER_DAILY_TARGET] ?: 2500 // Varsayılan 2500ml
        }

    /**
     * Su hatırlatıcısı aktif/pasif
     */
    suspend fun setWaterReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WATER_REMINDER_ENABLED] = enabled
        }
    }

    val waterReminderEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_WATER_REMINDER_ENABLED] ?: false
        }

    /**
     * Hatırlatıcı başlangıç saati (0-23)
     */
    suspend fun setWaterReminderStartHour(hour: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WATER_REMINDER_START_HOUR] = hour
        }
    }

    val waterReminderStartHour: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_WATER_REMINDER_START_HOUR] ?: 9 // Varsayılan 09:00
        }

    /**
     * Hatırlatıcı bitiş saati (0-23)
     */
    suspend fun setWaterReminderEndHour(hour: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WATER_REMINDER_END_HOUR] = hour
        }
    }

    val waterReminderEndHour: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_WATER_REMINDER_END_HOUR] ?: 22 // Varsayılan 22:00
        }

    /**
     * Hatırlatıcı sıklığı (dakika cinsinden)
     */
    suspend fun setWaterReminderFrequency(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WATER_REMINDER_FREQUENCY] = minutes
        }
    }

    val waterReminderFrequency: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_WATER_REMINDER_FREQUENCY] ?: 60 // Varsayılan her saat
        }

    // === ONBOARDING ===

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] ?: false
        }

    // === DİĞER METODLAR ===

    /**
     * Tüm ayarları sıfırla
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // === BACKWARD COMPATIBILITY ===

    suspend fun getReminderHourImmediate(): Int {
        return reminderHour.first()
    }

    suspend fun getReminderMinuteImmediate(): Int {
        return reminderMinute.first()
    }

    suspend fun isReminderEnabledImmediate(): Boolean {
        return isReminderEnabled.first()
    }
}