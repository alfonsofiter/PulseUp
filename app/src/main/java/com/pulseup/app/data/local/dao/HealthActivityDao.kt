package com.pulseup.app.data.local.dao

import androidx.room.*
import com.pulseup.app.data.local.entity.ActivityCategory
import com.pulseup.app.data.local.entity.HealthActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthActivityDao {
    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: HealthActivity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<HealthActivity>)

    // READ
    @Query("SELECT * FROM health_activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: Int): HealthActivity?

    // DIUBAH: userId menjadi String (Firebase UID)
    @Query("SELECT * FROM health_activities WHERE userId = :userId ORDER BY timestamp DESC")
    fun getActivitiesByUser(userId: String): Flow<List<HealthActivity>>

    @Query("SELECT * FROM health_activities WHERE userId = :userId AND category = :category ORDER BY timestamp DESC")
    fun getActivitiesByCategory(userId: String, category: ActivityCategory): Flow<List<HealthActivity>>

    @Query("SELECT * FROM health_activities WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentActivities(userId: String, limit: Int): Flow<List<HealthActivity>>

    @Query("SELECT * FROM health_activities WHERE userId = :userId AND timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getActivitiesToday(userId: String, startOfDay: Long): Flow<List<HealthActivity>>

    @Query("SELECT * FROM health_activities WHERE userId = :userId AND timestamp >= :startOfWeek ORDER BY timestamp DESC")
    fun getActivitiesThisWeek(userId: String, startOfWeek: Long): Flow<List<HealthActivity>>

    @Query("SELECT * FROM health_activities WHERE userId = :userId AND timestamp >= :startOfMonth ORDER BY timestamp DESC")
    fun getActivitiesThisMonth(userId: String, startOfMonth: Long): Flow<List<HealthActivity>>

    // UPDATE
    @Update
    suspend fun updateActivity(activity: HealthActivity)

    // DELETE
    @Delete
    suspend fun deleteActivity(activity: HealthActivity)

    @Query("DELETE FROM health_activities WHERE id = :activityId")
    suspend fun deleteActivityById(activityId: Int)

    @Query("DELETE FROM health_activities WHERE userId = :userId")
    suspend fun deleteAllActivitiesForUser(userId: String)

    // STATISTICS
    @Query("SELECT COUNT(*) FROM health_activities WHERE userId = :userId")
    suspend fun getTotalActivityCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM health_activities WHERE userId = :userId AND category = :category")
    suspend fun getActivityCountByCategory(userId: String, category: ActivityCategory): Int

    @Query("SELECT SUM(points) FROM health_activities WHERE userId = :userId")
    suspend fun getTotalPoints(userId: String): Int

    @Query("SELECT SUM(caloriesBurned) FROM health_activities WHERE userId = :userId")
    suspend fun getTotalCaloriesBurned(userId: String): Int

    @Query("SELECT SUM(points) FROM health_activities WHERE userId = :userId AND timestamp >= :startOfDay")
    suspend fun getPointsToday(userId: String, startOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM health_activities WHERE userId = :userId AND timestamp >= :startOfDay")
    suspend fun getActivityCountToday(userId: String, startOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM health_activities WHERE userId = :userId AND timestamp >= :startTime AND timestamp < :endTime")
    suspend fun getActivityCountBetween(userId: String, startTime: Long, endTime: Long): Int
}
