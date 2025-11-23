package com.example.naifdeneme

/**
 * Uygulama sabitleri
 * Magic number'ları buraya topluyoruz
 */
object Constants {

    // Navigation
    object Navigation {
        const val EXTRA_NAVIGATE_TO = "navigate_to"
        const val DESTINATION_MAIN = "main"
        const val DESTINATION_WATER = "water"
        const val DESTINATION_HABITS = "habits"
        const val DESTINATION_FINANCE = "finance"
        const val DESTINATION_NOTES = "notes"
        const val DESTINATION_POMODORO = "pomodoro"
        const val DESTINATION_SETTINGS = "settings"
    }

    // Water Tracker
    object Water {
        const val MIN_AMOUNT = 100
        const val MAX_AMOUNT = 5000
        const val MIN_DAILY_TARGET = 500
        const val MAX_DAILY_TARGET = 10000
        const val DEFAULT_DAILY_TARGET = 2500

        const val MIN_REMINDER_FREQUENCY = 15 // minutes
        const val MAX_REMINDER_FREQUENCY = 180 // minutes
        const val DEFAULT_REMINDER_FREQUENCY = 60

        const val DEFAULT_START_HOUR = 8
        const val DEFAULT_END_HOUR = 22
    }

    // Notification IDs
    object NotificationId {
        const val WATER_REMINDER_BASE = 1000
        const val HABIT_REMINDER_BASE = 2000
        const val MEDICINE_REMINDER_BASE = 3000
        const val TEST_NOTIFICATION = 9999
    }

    // WorkManager
    object Work {
        const val WATER_REMINDER_WORK = "water_reminder_work"
        const val HABIT_REMINDER_WORK_PREFIX = "habit_reminder_"
        const val TEST_REMINDER_WORK = "test_reminder"
    }
}