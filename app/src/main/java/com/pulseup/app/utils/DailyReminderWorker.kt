package com.pulseup.app.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore by preferencesDataStore(name = "notifications")

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val dailyReminderKey = booleanPreferencesKey("daily_reminder")
        val preferences = applicationContext.dataStore.data.first()
        val isEnabled = preferences[dailyReminderKey] ?: true

        if (isEnabled) {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showNotification(
                "Waktunya Bergerak!",
                "Jangan lupa log aktivitas kesehatanmu hari ini di PulseUp."
            )
        }

        return Result.success()
    }
}