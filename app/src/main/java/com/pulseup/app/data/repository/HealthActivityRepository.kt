package com.pulseup.app.data.repository

import com.pulseup.app.data.local.dao.HealthActivityDao
import com.pulseup.app.data.local.entity.ActivityCategory
import com.pulseup.app.data.local.entity.HealthActivity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class HealthActivityRepository(private val activityDao: HealthActivityDao) {

    // Get all activities by user
    fun getActivitiesByUser(userId: Int): Flow<List<HealthActivity>> =
        activityDao.getActivitiesByUser(userId)

    // Get activities by category
    fun getActivitiesByCategory(userId: Int, category: ActivityCategory): Flow<List<HealthActivity>> =
        activityDao.getActivitiesByCategory(userId, category)

    // Get recent activities
    fun getRecentActivities(userId: Int, limit: Int): Flow<List<HealthActivity>> =
        activityDao.getRecentActivities(userId, limit)

    // Get activities today
    fun getActivitiesToday(userId: Int): Flow<List<HealthActivity>> {
        val startOfDay = getStartOfDay()
        return activityDao.getActivitiesToday(userId, startOfDay)
    }

    // Get activities this week
    fun getActivitiesThisWeek(userId: Int): Flow<List<HealthActivity>> {
        val startOfWeek = getStartOfWeek()
        return activityDao.getActivitiesThisWeek(userId, startOfWeek)
    }

    // Get activities this month
    fun getActivitiesThisMonth(userId: Int): Flow<List<HealthActivity>> {
        val startOfMonth = getStartOfMonth()
        return activityDao.getActivitiesThisMonth(userId, startOfMonth)
    }

    // Insert activity
    suspend fun insertActivity(activity: HealthActivity): Long =
        activityDao.insertActivity(activity)

    // Update activity
    suspend fun updateActivity(activity: HealthActivity) =
        activityDao.updateActivity(activity)

    // Delete activity
    suspend fun deleteActivity(activity: HealthActivity) =
        activityDao.deleteActivity(activity)

    // Delete activity by ID
    suspend fun deleteActivityById(activityId: Int) =
        activityDao.deleteActivityById(activityId)

    // Get activity by ID
    suspend fun getActivityById(activityId: Int): HealthActivity? =
        activityDao.getActivityById(activityId)

    // Statistics
    suspend fun getTotalActivityCount(userId: Int): Int =
        activityDao.getTotalActivityCount(userId)

    suspend fun getActivityCountByCategory(userId: Int, category: ActivityCategory): Int =
        activityDao.getActivityCountByCategory(userId, category)

    suspend fun getTotalPoints(userId: Int): Int =
        activityDao.getTotalPoints(userId)

    suspend fun getTotalCaloriesBurned(userId: Int): Int =
        activityDao.getTotalCaloriesBurned(userId)

    suspend fun getPointsToday(userId: Int): Int {
        val startOfDay = getStartOfDay()
        return activityDao.getPointsToday(userId, startOfDay)
    }

    suspend fun getActivityCountToday(userId: Int): Int {
        val startOfDay = getStartOfDay()
        return activityDao.getActivityCountToday(userId, startOfDay)
    }

    // Helper functions for time calculations
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