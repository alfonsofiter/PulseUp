package com.pulseup.app.data.repository

import com.pulseup.app.data.local.dao.HealthActivityDao
import com.pulseup.app.data.local.entity.ActivityCategory
import com.pulseup.app.data.local.entity.HealthActivity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class HealthActivityRepository(private val activityDao: HealthActivityDao) {

    fun getActivitiesByUser(userId: String): Flow<List<HealthActivity>> =
        activityDao.getActivitiesByUser(userId)

    fun getActivitiesByCategory(userId: String, category: ActivityCategory): Flow<List<HealthActivity>> =
        activityDao.getActivitiesByCategory(userId, category)

    fun getRecentActivities(userId: String, limit: Int): Flow<List<HealthActivity>> =
        activityDao.getRecentActivities(userId, limit)

    fun getActivitiesToday(userId: String): Flow<List<HealthActivity>> {
        val startOfDay = getStartOfDay()
        return activityDao.getActivitiesToday(userId, startOfDay)
    }

    fun getActivitiesThisWeek(userId: String): Flow<List<HealthActivity>> {
        val startOfWeek = getStartOfWeek()
        return activityDao.getActivitiesThisWeek(userId, startOfWeek)
    }

    fun getActivitiesThisMonth(userId: String): Flow<List<HealthActivity>> {
        val startOfMonth = getStartOfMonth()
        return activityDao.getActivitiesThisMonth(userId, startOfMonth)
    }

    suspend fun insertActivity(activity: HealthActivity): Long =
        activityDao.insertActivity(activity)

    suspend fun updateActivity(activity: HealthActivity) =
        activityDao.updateActivity(activity)

    suspend fun deleteActivity(activity: HealthActivity) =
        activityDao.deleteActivity(activity)

    suspend fun deleteActivityById(activityId: Int) =
        activityDao.deleteActivityById(activityId)

    suspend fun getActivityById(activityId: Int): HealthActivity? =
        activityDao.getActivityById(activityId)

    suspend fun getTotalActivityCount(userId: String): Int =
        activityDao.getTotalActivityCount(userId)

    suspend fun getActivityCountByCategory(userId: String, category: ActivityCategory): Int =
        activityDao.getActivityCountByCategory(userId, category)

    suspend fun getTotalPoints(userId: String): Int =
        activityDao.getTotalPoints(userId)

    suspend fun getTotalCaloriesBurned(userId: String): Int =
        activityDao.getTotalCaloriesBurned(userId)

    suspend fun getPointsToday(userId: String): Int {
        val startOfDay = getStartOfDay()
        return activityDao.getPointsToday(userId, startOfDay)
    }

    suspend fun getActivityCountToday(userId: String): Int {
        val startOfDay = getStartOfDay()
        return activityDao.getActivityCountToday(userId, startOfDay)
    }

    // New method for streak logic
    suspend fun getActivityCountBetween(userId: String, startTime: Long, endTime: Long): Int {
        return activityDao.getActivityCountBetween(userId, startTime, endTime)
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfWeek(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
