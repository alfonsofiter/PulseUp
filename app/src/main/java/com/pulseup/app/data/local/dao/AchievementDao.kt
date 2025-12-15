package com.pulseup.app.data.local.dao

import androidx.room.*
import com.pulseup.app.data.local.entity.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    // READ
    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    suspend fun getAchievementById(achievementId: Int): Achievement?

    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedAt DESC")
    fun getAchievementsByUser(userId: Int): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE userId = :userId AND badgeId = :badgeId")
    suspend fun checkIfBadgeUnlocked(userId: Int, badgeId: Int): Achievement?

    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedAt DESC LIMIT :limit")
    fun getRecentAchievements(userId: Int, limit: Int): Flow<List<Achievement>>

    // UPDATE
    @Update
    suspend fun updateAchievement(achievement: Achievement)

    // DELETE
    @Delete
    suspend fun deleteAchievement(achievement: Achievement)

    @Query("DELETE FROM achievements WHERE userId = :userId")
    suspend fun deleteAllAchievementsForUser(userId: Int)

    // STATISTICS
    @Query("SELECT COUNT(*) FROM achievements WHERE userId = :userId")
    suspend fun getAchievementCount(userId: Int): Int

    @Query("SELECT COUNT(DISTINCT badgeId) FROM achievements WHERE userId = :userId")
    suspend fun getUniqueBadgeCount(userId: Int): Int
}