package com.example.naifdeneme

import android.app.Application
import android.util.Log
import androidx.work.Configuration

class ModaiApplication : Application(), Configuration.Provider {

    companion object {
        private const val TAG = "ModaiApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")

        initializeNotificationChannels()

        Log.i(TAG, "Application initialized successfully")
    }

    private fun initializeNotificationChannels() {
        try {
            NotificationHelper.createNotificationChannels(this)
            Log.d(TAG, "Notification channels created")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create notification channels", e)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(
                if (BuildConfig.DEBUG)
                    android.util.Log.DEBUG
                else
                    android.util.Log.ERROR
            )
            .build()
}