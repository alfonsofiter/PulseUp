package com.pulseup.app.utils

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.util.*

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)
        val type = inputData.getString("type") ?: return Result.failure()

        when (type) {
            "HYDRATION" -> {
                notificationHelper.showNotification(
                    "💧 Waktunya Minum!",
                    "Jangan lupa minum air agar tubuh tetap terhidrasi dan bugar."
                )
            }
            "MORNING" -> {
                notificationHelper.showNotification(
                    "🌅 Semangat Pagi!",
                    "Ayo mulai harimu dengan energi positif. Siap untuk PulseUp hari ini?"
                )
            }
            "EVENING" -> {
                notificationHelper.showNotification(
                    "🌙 Evaluasi Hari Ini",
                    "Sudahkah kamu mencatat aktivitasmu hari ini? Jangan biarkan streak-mu putus!"
                )
            }
        }

        return Result.success()
    }

    companion object {
        fun scheduleNotifications(context: Context) {
            val workManager = WorkManager.getInstance(context)

            // 1. Hydration Reminder (Setiap 1 Jam)
            val hydrationRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.HOURS)
                .setInputData(workDataOf("type" to "HYDRATION"))
                .addTag("hydration_work")
                .build()

            workManager.enqueueUniquePeriodicWork(
                "HydrationWork",
                ExistingPeriodicWorkPolicy.KEEP,
                hydrationRequest
            )

            // 2. Morning Reminder (Jam 07.00)
            scheduleDailyWork(workManager, "MORNING", 7, 0, "MorningWork")

            // 3. Evening Reminder (Jam 20.00)
            scheduleDailyWork(workManager, "EVENING", 20, 0, "EveningWork")
        }

        private fun scheduleDailyWork(
            workManager: WorkManager,
            type: String,
            hour: Int,
            minute: Int,
            uniqueName: String
        ) {
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            val delay = calendar.timeInMillis - now

            val dailyRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("type" to type))
                .addTag(uniqueName)
                .build()

            workManager.enqueueUniqueWork(
                uniqueName,
                ExistingWorkPolicy.REPLACE,
                dailyRequest
            )
        }
    }
}
